package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.utils.testcase.TestCaseOptimizer;
import org.mate.utils.testcase.TestCaseStatistics;
import org.mate.utils.testcase.serialization.TestCaseSerializer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEReplayRun {

    @Test
    public void useAppContext() {
        MATE.log_acc("Starting ReplayRun...");

        // init uiAbstractionLayer, properties, etc.
        MATE mate = new MATE();

        Registry.registerReplayMode();

        MATE.log_acc("Relative Intent Amount: " + Properties.RELATIVE_INTENT_AMOUNT());

        // track which test cases couldn't be successfully replayed
        Map<Integer, TestCase> failures = new TreeMap<>();

        int testCaseID = 0;

        TestCase testCase = TestCaseSerializer.deserializeTestCase();

        // reset the app once
        Registry.getUiAbstractionLayer().resetApp();

        // as long as we find a test case for replaying
        while (testCase != null) {

            MATE.log("Replaying TestCase " + testCaseID);

            // apply optimisation strategy before replaying (optional)
            testCase = TestCaseOptimizer.optimise(testCase);

            if (replayTestCase(testCase)) {
                MATE.log("Replayed TestCase " + testCaseID);
                // record stats only if test case could be successfully replayed
                TestCaseStatistics.recordStats(testCase);
            } else {
                failures.put(testCaseID, testCase);
            }

            // replay next test case
            testCase = TestCaseSerializer.deserializeTestCase();

            testCaseID++;

            // reset aut after each test case
            Registry.getUiAbstractionLayer().resetApp();
        }

        MATE.log("Retry replaying " + failures.size() + " test cases!");
        MATE.log("Retry replaying test cases: " + failures.keySet());

        // track which test cases couldn't be replayed though retry
        Set<Integer> nonRecoverableTestCases = new TreeSet<>(failures.keySet());

        // retry failed test cases
        for (Map.Entry<Integer, TestCase> entry : failures.entrySet()) {

            boolean success = false;

            for (int i = 0; i < 5 && !success; i++) {

                MATE.log("Replaying TestCase " + entry.getKey());

                // TODO: we could try to insert some artificial delay between the actions
                //  since the AUT might be too slow on loading on the current activity
                //  however this should be only done if we are on the expected activity
                success = replayTestCase(entry.getValue());

                if (success) {
                    MATE.log("Replayed TestCase " + entry.getKey());
                    nonRecoverableTestCases.remove(entry.getKey());
                    // record stats about successful test cases
                    TestCaseStatistics.recordStats(entry.getValue());
                }

                // reset aut after each test case
                Registry.getUiAbstractionLayer().resetApp();
            }
        }

        MATE.log("Number of non recoverable test cases: " + nonRecoverableTestCases.size());
        MATE.log("Non recoverable test cases: " + nonRecoverableTestCases);
    }

    /**
     * Replays a test case. Repairs individual UI actions if not directly applicable.
     *
     * @param testCase The test case to be replayed.
     * @return Returns {@code true} if the test case could be successfully replayed,
     * otherwise {@code false} is returned.
     */
    private boolean replayTestCase(TestCase testCase) {

        // get the actions for replaying
        List<Action> actions = testCase.getEventSequence();

        for (int i = 0; i < testCase.getEventSequence().size(); i++) {

            // TODO: track for how many actions current and expected activity diverge
            MATE.log("Current Activity: " + Registry.getUiAbstractionLayer().getCurrentActivity());
            MATE.log("Expected Activity: " + testCase.getActivityBeforeAction(i));

            // TODO: one should abort if a (primitive) action leaves the app, because there must be some divergence
            Action nextAction = actions.get(i);

            // check whether the UI action is applicable on the current state
            if ((nextAction instanceof WidgetAction || (nextAction instanceof MotifAction
                    && Properties.WIDGET_BASED_ACTIONS()))
                    && !Registry.getUiAbstractionLayer().getExecutableActions().contains(nextAction)) {

                // try to repair UI action
                Action repairedAction = repairUIAction(nextAction);

                if (repairedAction != null) {
                    MATE.log("replaying action " + i + ": " + repairedAction);
                    Registry.getUiAbstractionLayer().executeAction(repairedAction);
                    MATE.log("replayed action " + i + ": " + repairedAction);
                } else {
                    MATE.log("Action not applicable!");
                    return false;
                }
            } else {
                MATE.log("replaying action " + i + ": " + nextAction);
                Registry.getUiAbstractionLayer().executeAction(nextAction);
                MATE.log("replayed action " + i + ": " + nextAction);
            }
        }
        return true;
    }

    /**
     * If a de-serialized (widget-based) action is not applicable to the current state,
     * we can try to select an alternative action.
     *
     * @param a The action not applicable on the current state.
     * @return Returns an alternative action that is applicable, or {@code null} if no appropriate
     * action could be derived.
     */
    private Action repairUIAction(Action a) {

        // TODO: provide appropriate repair mechanism!

        // log information about selected and available actions
        if (a instanceof WidgetAction && !Registry.getUiAbstractionLayer().getExecutableActions().contains(a)) {

            WidgetAction selectedAction = (WidgetAction) a;

            MATE.log(selectedAction.getActionType() + " on " + selectedAction.getWidget().getId()
                    + " Text : " + selectedAction.getWidget().getText()
                    + " hint : " + selectedAction.getWidget().getHint()
                    + " Class : " + selectedAction.getWidget().getClazz()
                    + " ResourceID : " + selectedAction.getWidget().getResourceID()
                    + " X : " + selectedAction.getWidget().getX()
                    + " Y : " + selectedAction.getWidget().getY());

            MATE.log("------------------------------------------");
            MATE.log("Applicable widget actions with same action type: ");

            for (Action action : Registry.getUiAbstractionLayer().getExecutableActions()) {

                if (action instanceof WidgetAction) {
                    if (((WidgetAction) action).getActionType() == selectedAction.getActionType()) {
                        WidgetAction widgetAction = (WidgetAction) action;
                        MATE.log(widgetAction.getActionType() + " on " + widgetAction.getWidget().getId()
                                + " Text : " + widgetAction.getWidget().getText()
                                + " hint : " + widgetAction.getWidget().getHint()
                                + " Class : " + widgetAction.getWidget().getClazz()
                                + " ResourceID : " + widgetAction.getWidget().getResourceID()
                                + " X : " + widgetAction.getWidget().getX()
                                + " Y : " + widgetAction.getWidget().getY());
                    }
                }
            }
        } else if (a instanceof MotifAction && Properties.WIDGET_BASED_ACTIONS()
                && !Registry.getUiAbstractionLayer().getExecutableActions().contains(a)) {

            MotifAction selectedAction = (MotifAction) a;

            MATE.log(selectedAction.getActionType() + "on " + selectedAction.getActivityName());

            for (UIAction widgetAction : selectedAction.getUIActions()) {
                MATE.log(widgetAction.toString());
            }

            MATE.log("------------------------------------------");
            MATE.log("Applicable motif actions with same action type: ");

            for (Action action : Registry.getUiAbstractionLayer().getExecutableActions()) {

                if (action instanceof MotifAction) {
                    MotifAction motifAction = (MotifAction) action;
                    if (motifAction.getActionType() == selectedAction.getActionType()) {

                        MATE.log(motifAction.getActionType() + "on " + motifAction.getActivityName());

                        for (UIAction widgetAction : motifAction.getUIActions()) {
                            MATE.log(widgetAction.toString());
                        }
                    }
                }
            }
        }
        return null;
    }
}
