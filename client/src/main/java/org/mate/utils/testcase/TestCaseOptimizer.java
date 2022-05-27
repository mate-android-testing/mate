package org.mate.utils.testcase;

import org.mate.Properties;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.intent.IntentBasedAction;
import org.mate.commons.interaction.action.intent.SystemAction;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.manifest.element.ComponentType;
import org.mate.model.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides different optimisation strategies for a {@link org.mate.model.TestCase}.
 * This can be essentially anything from removing all actions of kind
 * {@link org.mate.commons.interaction.action.intent.IntentBasedAction} to dropping solely individual
 * actions.
 */
public final class TestCaseOptimizer {

    private TestCaseOptimizer() {
        throw new UnsupportedOperationException("Utility class not instantiable!");
    }

    /**
     * Performs the selected test case optimisation strategy.
     *
     * @param testCase The test case to be optimised.
     * @return Returns the optimised test case.
     */
    public static TestCase optimise(TestCase testCase) {

        switch (Properties.OPTIMISATION_STRATEGY()) {
            case REMOVE_ALL_INTENT_ACTIONS:
                // remove all intent based actions
                return removeAllIntentBasedActions(testCase);
            case REMOVE_ALL_EXCEPT_LAST_ACTION:
                // remove all except last action
                return removeAllExceptLastAction(testCase);
            case REMOVE_ALL_ACTIONS_BEFORE_LAST_ACTIVITY_TRANSITION:
                // execute all actions after last activity transition
                return removeAllActionsBeforeLastActivityTransition(testCase);
            case REMOVE_ALL_NON_ACTIVITY_RELATED_ACTIONS:
                // solely activity related actions (UI + intent)
                return removeAllNonActivityRelatedActions(testCase);
            case REMOVE_ALL_UI_ACTIONS:
                // remove all UI actions
                return removeAllUIActions(testCase);
            default:
                // leave the test case unchanged
                return testCase;
        }
    }

    /**
     * The first optimisation strategy.
     *
     * Removes all intent-based actions (both intent and system actions) from the test case.
     *
     * @param testCase The test case to be optimised.
     * @return Returns the optimised test case.
     */
    private static TestCase removeAllIntentBasedActions(TestCase testCase) {

        List<Action> toBeRemoved = new ArrayList<>();
        List<Action> actions = testCase.getActionSequence();

        for (Action action : actions) {
            if (action instanceof IntentBasedAction || action instanceof SystemAction) {
                toBeRemoved.add(action);
            }
        }

        actions.removeAll(toBeRemoved);
        return testCase;
    }

    /**
     * The second optimisation strategy.
     *
     * Removes all actions of a test case except the last one. The idea is that
     * a {@link org.mate.commons.interaction.action.intent.IntentBasedAction} may lead to a crash
     * directly without preceding UI or Intent-based actions. If no crash occurs anymore,
     * the last action didn't cause the crash, at least not independently.
     *
     * @param testCase The given test case.
     * @return Returns the test case containing solely the last action.
     */
    private static TestCase removeAllExceptLastAction(TestCase testCase) {

        List<Action> toBeRemoved = new ArrayList<>();

        for (int i = 0; i < testCase.getActionSequence().size() - 1; i++) {
            toBeRemoved.add(testCase.getActionSequence().get(i));
        }

        testCase.getActionSequence().removeAll(toBeRemoved);
        return testCase;
    }

    /**
     * The third optimisation strategy.
     *
     * Only execute those actions after the last activity transition.
     *
     * @param testCase The test case to be optimised.
     * @return Returns the optimised test case.
     */
    private static TestCase removeAllActionsBeforeLastActivityTransition(TestCase testCase) {

        if (testCase.getActionSequence().isEmpty()) {
            // no actions -> no activity transitions
            return testCase;
        }

        List<Action> toBeRemoved = new ArrayList<>();
        List<Action> actions = new ArrayList<>(testCase.getActionSequence());

        // traverse backwards until we reach a different activity
        int index = testCase.getActionSequence().size() - 1;
        String activity = testCase.getActivityAfterAction(index);
        index--;

        // traverse backwards until we reach a different activity
        while (index >= 0) {

            if (!testCase.getActivityAfterAction(index).equals(activity)) {
                // we reached a different activity
                break;
            }
            index--;
        }

        // collect all actions up to last activity transition for removal
        for (int i = 0; i <= index; i++) {
            toBeRemoved.add(actions.get(i));
        }

        testCase.getActionSequence().removeAll(toBeRemoved);
        return testCase;
    }


    /**
     * The fourth optimisation strategy.
     *
     * Removes all actions that do not target an activity.
     *
     * @param testCase The test case to be optimised.
     * @return Returns the optimised test case.
     */
    private static TestCase removeAllNonActivityRelatedActions(TestCase testCase) {

        List<Action> toBeRemoved = new ArrayList<>();
        List<Action> actions = testCase.getActionSequence();

        for (Action action : actions) {
            if (action instanceof UIAction) {
                continue;
            } else if (action instanceof IntentBasedAction
                    && ((IntentBasedAction) action).getComponentType() == ComponentType.ACTIVITY) {
                continue;
            } else {
                toBeRemoved.add(action);
            }
        }

        actions.removeAll(toBeRemoved);
        return testCase;
    }

    /**
     * The fifth optimisation strategy.
     *
     * Removes all UI actions from the test case.
     *
     * @param testCase The test case to be optimised.
     * @return Returns the optimised test case.
     */
    private static TestCase removeAllUIActions(TestCase testCase) {

        List<Action> toBeRemoved = new ArrayList<>();
        List<Action> actions = testCase.getActionSequence();

        for (Action action : actions) {
            if (action instanceof UIAction) {
                toBeRemoved.add(action);
            }
        }

        actions.removeAll(toBeRemoved);
        return testCase;
    }

    /**
     * Removes from the given test case the last {@param n} actions of type {@param actionType}.
     *
     * @param testCase   The test case to be optimised.
     * @param n          The number of intent-based actions that should be removed (upper bound), n > 0.
     * @param actionType The type of action, either an intent-based action, a system action
     *                   or a UI action.
     * @return Returns the test case after applied the optimisation.
     */
    private static TestCase removeLastActions(TestCase testCase, int n, Class actionType) {

        MATELog.log("Optimising TestCase!");

        List<Action> actions = new ArrayList<>(testCase.getActionSequence());
        Collections.reverse(actions);

        List<Action> toBeRemoved = new ArrayList<>(n);
        int ctr = 0;

        for (Action action : actions) {

            if (ctr == n) {
                // we reached the number of elements we want to remove
                break;
            }

            if (action.getClass().equals(actionType)) {
                toBeRemoved.add(action);
                ctr++;
            }
        }

        MATELog.log("Removed from TestCase " + ctr + " " + actionType.getName() + "!");

        // remove the actions from the test case
        testCase.getActionSequence().removeAll(toBeRemoved);
        return testCase;
    }

    /**
     * Removes the action at the specified {@param index}.
     *
     * @param testCase The given test case.
     * @param index    The index of the action which should be removed.
     * @return Returns the test case without the action at the given index.
     */
    private static TestCase removeActionAtIndex(TestCase testCase, int index) {
        testCase.getActionSequence().remove(index);
        return testCase;
    }

    /**
     * Removes the last action of a test case.
     *
     * @param testCase The given test case.
     * @return Returns the test case without the last action.
     */
    private static TestCase removeLastAction(TestCase testCase) {

        int lastIndex = testCase.getActionSequence().size() - 1;
        testCase.getActionSequence().remove(lastIndex);
        return testCase;
    }

    /**
     * Checks whether the last action of the given test case is of type {@param type}.
     *
     * @param testCase The given test case.
     * @param type     The action type, e.g. {@link org.mate.commons.interaction.action.intent.IntentBasedAction}.
     * @return Returns {@code true} if the last action matches the given action type,
     * otherwise {@code false} is returned.
     */
    private static boolean isLastActionOfGivenType(TestCase testCase, Class type) {

        int lastIndex = testCase.getActionSequence().size() - 1;
        return testCase.getActionSequence().get(lastIndex).getClass().equals(type);
    }



}
