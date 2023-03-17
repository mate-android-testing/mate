package org.mate.model;

import android.support.annotation.NonNull;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
import org.mate.interaction.action.ui.PrimitiveAction;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.util.DotConverter;
import org.mate.state.IScreenState;
import org.mate.utils.ListUtils;
import org.mate.utils.Optional;
import org.mate.utils.Randomness;
import org.mate.utils.StackTrace;
import org.mate.utils.testcase.TestCaseStatistics;
import org.mate.utils.testcase.espresso.EspressoConverter;
import org.mate.utils.testcase.serialization.TestCaseSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestCase {

    /**
     * A random generated id that uniquely identifies the test case.
     * Also used as the string representation.
     */
    private String id;

    /**
     * The states (ids) in the order they were visited.
     */
    private final List<String> stateSequence;

    /**
     * The actions that has been executed by this test case.
     */
    private final List<Action> actionSequence;

    /**
     * The visited activities in the order they appeared.
     */
    private final List<String> activitySequence;

    /**
     * Whether a crash has been triggered by an action of the test case.
     */
    private boolean crashDetected;

    /**
     * The desired size of the test case, i.e. the desired length
     * of the test case. This doesn't enforce any size restriction yet.
     */
    private Optional<Integer> desiredSize = Optional.none();

    /**
     * The stack trace that has been triggered by a potential crash.
     * Only recorded when {@link org.mate.Properties#RECORD_STACK_TRACE()} is defined.
     */
    private StackTrace crashStackTrace = null;

    /**
     * Should be used for the creation of dummy test cases.
     * This suppresses the log that indicates a new test case
     * for the AndroidAnalysis framework.
     */
    private TestCase() {
        setId("dummy");
        crashDetected = false;
        stateSequence = new ArrayList<>();
        actionSequence = new ArrayList<>();
        activitySequence = new ArrayList<>();
    }

    /**
     * Creates a new test case object with the given id.
     *
     * @param id The (unique) test case id.
     */
    public TestCase(String id) {
        MATE.log("Initialising new test case!");
        setId(id);
        crashDetected = false;
        stateSequence = new ArrayList<>();
        actionSequence = new ArrayList<>();
        activitySequence = new ArrayList<>();

        /*
        * In rare circumstances, test cases without any action are created. This, however, means
        * that updateTestCase() was never called, thus the state and activity sequence is empty,
        * which in turn may falsify activity coverage and the gui model.
         */
        IScreenState lastScreenState = Registry.getUiAbstractionLayer().getLastScreenState();
        stateSequence.add(lastScreenState.getId());
        activitySequence.add(lastScreenState.getActivityName());
    }

    /**
     * Checks whether this is a dummy test case.
     *
     * @return Returns {@code true} when this test case is a dummy test case,
     *         otherwise {@code false} is returned.
     */
    public boolean isDummy() {
        return getId().equals("dummy");
    }

    /**
     * Should be called (once) after the test case has been created and executed.
     *
     * Among other things, this method is responsible for the serialization
     * of a test case (if desired), the recording of test case stats (if desired)
     * and so on.
     */
    public void finish() {
        MATE.log("Finishing test case!");

        MATE.log("Found crash: " + hasCrashDetected());

        // serialization of test case
        if (Properties.RECORD_TEST_CASE()) {
            TestCaseSerializer.serializeTestCase(this);
        }

        if (Properties.CONVERT_GUI_TO_DOT() == DotConverter.Option.ALL) {
            DotConverter.convertTestcase(Registry.getUiAbstractionLayer().getGuiModel(), this);
        }

        // convert test case to reproducible espresso test
        if (Properties.CONVERT_TEST_CASE_TO_ESPRESSO_TEST()) {
            EspressoConverter.convert(this);
        }

        // record stats about a test case, in particular about intent based actions
        if (Properties.RECORD_TEST_CASE_STATS()) {
            TestCaseStatistics.recordStats(this);
        }

        // TODO: log the test case actions in a proper format
    }

    /**
     * Returns the activity name before the execution of the given action.
     *
     * @param actionIndex The action index.
     * @return Returns the activity in foreground before the given action was executed.
     */
    public String getActivityBeforeAction(int actionIndex) {
        return activitySequence.get(actionIndex);
    }

    /**
     * Returns the name of the activity that is in the foreground after the execution
     * of the n-th {@param actionIndex} action.
     *
     * @param actionIndex The action index.
     * @return Returns the activity name after the execution of the {@param actionIndex} action.
     */
    public String getActivityAfterAction(int actionIndex) {
        // the activity sequence models a 'activity-before-action' relation
        return activitySequence.get(actionIndex + 1);
    }

    /**
     * Returns the activity sequence (in order) that has been covered through the execution
     * of the test case actions.
     *
     * @return Returns the activity sequence in the order they have been visited.
     */
    public List<String> getActivitySequence() {
        return activitySequence;
    }

    /**
     * Sets a desired length for the test case, i.e. the maximum
     * number of of actions. This doesn't enforce any size restriction yet.
     *
     * @param desiredSize A desired length for the test case.
     */
    public void setDesiredSize(Optional<Integer> desiredSize) {
        this.desiredSize = desiredSize;
    }

    /**
     * Returns the desired size for the test case, i.e. a desired
     * length of the test case.
     *
     * @return Returns the desired size.
     */
    @SuppressWarnings("unused")
    public Optional<Integer> getDesiredSize() {
        return desiredSize;
    }

    /**
     * Returns the unique id of the test case.
     *
     * @return Returns the test case id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the test case id to the given value.
     *
     * @param id The new test case id.
     */
    private void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the set of visited activities. This includes activities not belonging to the AUT.
     *
     * @return Returns the set of visited activities.
     */
    public Set<String> getVisitedActivities() {
        return ListUtils.toSet(activitySequence);
    }

    /**
     * Returns the set of visited activities belonging to the AUT.
     *
     * @return Returns the set of visited activities.
     */
    public Set<String> getVisitedActivitiesOfApp() {
        return getVisitedActivities().stream()
                .filter(activity -> Registry.getUiAbstractionLayer().getActivities().contains(activity))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the visited screen state sequence, actually the screen state ids.
     *
     * @return Returns the visited state sequence.
     */
    public List<String> getStateSequence() {
        return stateSequence;
    }

    /**
     * Returns the set of visited states, actually the screen state ids.
     *
     * @return Returns the set of visited states.
     */
    public Set<String> getVisitedStates() {
        return ListUtils.toSet(stateSequence);
    }

    /**
     * Returns the list of executed actions.
     *
     * @return Returns the action sequence.
     */
    public List<Action> getActionSequence() {
        return this.actionSequence;
    }

    /**
     * Checks whether the test case caused a crash.
     *
     * @return Returns {@code true} if the test case caused a crash,
     *         otherwise {@code false} is returned.
     */
    public boolean hasCrashDetected() {
        return this.crashDetected;
    }

    /**
     * Sets the crash flag.
     */
    public void setCrashDetected() {
        this.crashDetected = true;
    }

    /**
     * Returns the stack trace triggered by a crash of the test case.
     *
     * @return Returns the stack trace caused by the test case;
     *         this should be typically the last action.
     */
    @SuppressWarnings("unused")
    public StackTrace getCrashStackTrace() {
        if (Properties.RECORD_STACK_TRACE()) {
            return crashStackTrace;
        } else {
            throw new IllegalStateException("Recording stack trace is not enabled!");
        }
    }

    /**
     * Creates a dummy test case intended to be not used for execution.
     *
     * @return Returns a dummy test case.
     */
    public static TestCase newDummy() {
        return new TestCase();
    }

    /**
     * Creates a test case from a given dummy test case. This
     * causes the execution of actions declared by the dummy test case.
     *
     * @param testCase The dummy test case.
     * @return Returns a test case that executed the actions of the dummy.
     */
    public static TestCase fromDummy(TestCase testCase) {

        Registry.getUiAbstractionLayer().resetApp();
        TestCase resultingTc = newInitializedTestCase();

        int finalSize = testCase.actionSequence.size();

        if (testCase.desiredSize.hasValue()) {
            finalSize = testCase.desiredSize.getValue();
        }

        int count = 0;
        for (Action action0 : testCase.actionSequence) {
            if (count < finalSize) {
                if (!(action0 instanceof WidgetAction)
                        || Registry.getUiAbstractionLayer().getExecutableActions().contains(action0)) {
                    if (!resultingTc.updateTestCase(action0, count)) {
                        return resultingTc;
                    }
                    count++;
                } else {
                    break;
                }
            } else {
                return resultingTc;
            }
        }
        for (; count < finalSize; count++) {
            Action action;
            if (!Properties.USE_PRIMITIVE_ACTIONS()) {
                action = Randomness.randomElement(Registry.getUiAbstractionLayer().getExecutableActions());
            } else {
                action = PrimitiveAction.randomAction();
            }
            if (!resultingTc.updateTestCase(action, count)) {
                return resultingTc;
            }
        }

        return resultingTc;
    }

    /**
     * Returns the string representation of a test case.
     * This is the unique test case id for now.
     *
     * @return Returns the test case representation.
     */
    @NonNull
    @Override
    public String toString() {
        return getId();
    }

    /**
     * Initializes a new test case with a random id.
     *
     * @return Returns a new test case with a random id.
     */
    public static TestCase newInitializedTestCase() {
        return new TestCase(UUID.randomUUID().toString());
    }

    /**
     * Executes the given action and updates the test case accordingly.
     *
     * @param action The action to be executed.
     * @param actionID The id of the action.
     * @return Returns {@code true} if the given action didn't cause a crash of the app
     *         or left the AUT, otherwise {@code false} is returned.
     */
    public boolean updateTestCase(final Action action, final int actionID) {

        if (action instanceof WidgetAction
                && !Registry.getUiAbstractionLayer().getExecutableActions().contains(action)) {
            throw new IllegalStateException("Action not applicable to current state!");
        }

        IScreenState oldState = Registry.getUiAbstractionLayer().getLastScreenState();

        // If we use a surrogate model, we need to postpone the logging as we might predict wrong.
        if (!Properties.SURROGATE_MODEL()) {
            MATE.log("executing action " + actionID + ": " + action);
        }

        ActionResult actionResult = Registry.getUiAbstractionLayer().executeAction(action);

        // If we use a surrogate model, we need to postpone the logging as we might predict wrong.
        if (!Properties.SURROGATE_MODEL()) {

            IScreenState newState = Registry.getUiAbstractionLayer().getLastScreenState();

            // track the activity and state transition of each action
            String activityBeforeAction = oldState.getActivityName();
            String activityAfterAction = newState.getActivityName();
            String newStateID = newState.getId();

            if (actionResult == ActionResult.FAILURE_UIAUTOMATOR
                    || actionResult == ActionResult.FAILURE_UNKNOWN) {
                // We couldn't derive the target state.
                activityAfterAction = "unknown";
                newStateID = "unknown";
            }

            actionSequence.add(action);
            activitySequence.add(activityAfterAction);
            stateSequence.add(newStateID);

            MATE.log("executed action " + actionID + ": " + action);
            MATE.log("Activity Transition for action " + actionID
                    + ":" + activityBeforeAction + "->" + activityAfterAction);
        }

        switch (actionResult) {
            case SUCCESS:
                return true;
            case FAILURE_APP_CRASH:
                setCrashDetected();
                if (Properties.RECORD_STACK_TRACE()) {
                    crashStackTrace = Registry.getUiAbstractionLayer().getLastCrashStackTrace();
                }
            case SUCCESS_OUTBOUND:
                return false;
            case FAILURE_UIAUTOMATOR:
            case FAILURE_UNKNOWN:
                return false;
            default:
                throw new UnsupportedOperationException("Encountered an unknown action result. Cannot continue.");
        }
    }
}
