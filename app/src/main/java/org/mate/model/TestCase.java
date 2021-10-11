package org.mate.model;

import static org.mate.interaction.UIAbstractionLayer.ActionResult;

import android.support.annotation.NonNull;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.PrimitiveAction;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.deprecated.graph.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.utils.Optional;
import org.mate.utils.Randomness;
import org.mate.utils.testcase.TestCaseStatistics;
import org.mate.utils.testcase.serialization.TestCaseSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TestCase {

    /**
     * A random generated id that uniquely identifies the test case.
     * Also used as the string representation.
     */
    private String id;

    /**
     * The set of visited activities.
     */
    private final Set<String> visitedActivities;

    /**
     * The set of visited screen states (ids).
     */
    private final Set<String> visitedStates;

    /**
     * The actions that has been executed by this test case.
     */
    private final List<Action> eventSequence;

    /**
     * The visited activities in the order they appeared.
     */
    private final List<String> activitySequence;

    /**
     * A novelty score based on novelty search, not yet implemented.
     * Consider https://hal.archives-ouvertes.fr/hal-01121228/document as a reference.
     */
    private float novelty;

    /**
     * A sparseness value used for novelty search, not yet implemented.
     * Consider https://hal.archives-ouvertes.fr/hal-01121228/document as a reference.
     */
    private double sparseness;

    /**
     * Whether a crash has been triggered by an action of the test case.
     */
    private boolean crashDetected;

    /**
     * A mapping of a screen state (id) to an action (id).
     * The implementation is currently considered as deprecated.
     */
    private final Map<String, String> statesMap;

    /**
     * A feature vector that maps a screen state to the value 0 (unvisited) or 1 (visited).
     * The implementation is currently considered as deprecated.
     */
    private final Map<String, Integer> featureVector;

    /**
     * The desired size of the test case, i.e. the desired length
     * of the test case. This doesn't enforce any size restriction yet.
     */
    private Optional<Integer> desiredSize = Optional.none();

    /**
     * The stack trace that has been triggered by a potential crash.
     * Only recorded when {@link org.mate.Properties#RECORD_STACK_TRACE()} is defined.
     */
    private String crashStackTrace = null;

    /**
     * Should be used for the creation of dummy test cases.
     * This suppresses the log that indicates a new test case
     * for the AndroidAnalysis framework.
     */
    private TestCase() {
        setId("dummy");
        crashDetected = false;
        visitedActivities = new HashSet<>();
        visitedStates = new HashSet<>();
        eventSequence = new ArrayList<>();
        sparseness = 0;
        statesMap = new HashMap<>();
        featureVector = new HashMap<>();
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
        visitedActivities = new HashSet<>();
        visitedStates = new HashSet<>();
        eventSequence = new ArrayList<>();
        sparseness = 0;
        statesMap = new HashMap<>();
        featureVector = new HashMap<>();
        activitySequence = new ArrayList<>();
    }

    /**
     * Checks whether this is a dummy test case.
     *
     * @return Returns {@code true} when this test case is a dummy test case,
     *          otherwise {@code false} is returned.
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
    // TODO: ensure that finish() is properly called after each test case
    public void finish() {
        MATE.log("Finishing test case!");

        MATE.log("Found crash: " + getCrashDetected());

        // serialization of test case
        if (Properties.RECORD_TEST_CASE()) {
            TestCaseSerializer.serializeTestCase(this);
        }

        // record stats about a test case, in particular about intent based actions
        if (Properties.RECORD_TEST_CASE_STATS()) {
            TestCaseStatistics.recordStats(this);
        }

        MATE.log("Visited activities in order: " + activitySequence);

        // TODO: ensure that this log only appears here -> required for analysis framework
        MATE.log("Visited activities: " + getVisitedActivities());

        // TODO: log the test case actions in a proper format
    }

    /**
     * Returns the activity name before the execution of the given action.
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
     * Adds a new action to the list of executed actions.
     *
     * @param event The action to be added.
     */
    public void addEvent(Action event) {
        this.eventSequence.add(event);
    }

    /**
     * Updates the set of visited activities.
     *
     * @param activity A new activity to be added.
     */
    public void updateVisitedActivities(String activity) {
        this.visitedActivities.add(activity);
    }

    /**
     * Returns the set of visited activities.
     *
     * @return Returns the visited activities.
     */
    public Set<String> getVisitedActivities() {
        return visitedActivities;
    }

    /**
     * Updates the visited states with a new screen state.
     *
     * @param GUIState The new screen state.
     */
    public void updateVisitedStates(IScreenState GUIState) {
        this.visitedStates.add(GUIState.getId());
    }

    /**
     * Returns the visited screen states, actually the screen state ids.
     *
     * @return Returns the visited states.
     */
    public Set<String> getVisitedStates() {
        return visitedStates;
    }

    /**
     * Returns the list of executed actions.
     *
     * @return Returns the action sequence.
     */
    public List<Action> getEventSequence() {
        return this.eventSequence;
    }

    /**
     * Checks whether the test case caused a crash.
     *
     * @return Returns {@code true} if the test case caused a crash,
     *          otherwise {@code false} is returned.
     */
    public boolean getCrashDetected() {
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
     *          this should be typically the last action.
     */
    @SuppressWarnings("unused")
    public String getCrashStackTrace() {
        if (Properties.RECORD_STACK_TRACE()) {
            return crashStackTrace;
        } else {
            throw new IllegalStateException("Recording stack trace is not enabled!");
        }
    }

    /**
     * Sets a novelty value for the test case.
     *
     * @param novelty The new novelty score.
     */
    @SuppressWarnings("unused")
    public void setNovelty(float novelty) {
        this.novelty = novelty;
    }

    /**
     * Gets the novelty score of the test case.
     *
     * @return Returns the novelty score.
     */
    @SuppressWarnings("unused")
    public float getNovelty() {
        return novelty;
    }

    /**
     * Gets the sparseness of the test case.
     *
     * @return Returns the test case's sparseness.
     */
    @SuppressWarnings("unused")
    public double getSparseness() {
        return sparseness;
    }

    /**
     * Sets the sparseness of the test case.
     *
     * @param sparseness The new sparseness of the test case.
     */
    @SuppressWarnings("unused")
    public void setSparseness(double sparseness) {
        this.sparseness = sparseness;
    }

    /**
     * Updates the state map.
     *
     * @param state Represents the id of a screen state.
     * @param event Represents the id of an action.
     */
    @Deprecated
    public void updateStatesMap(String state, String event) {
        if (!statesMap.containsKey(state)){
            statesMap.put(state, event);
        }
    }

    /**
     * Returns a mapping of screen states to actions.
     *
     * @return Returns a screen state to action mapping.
     */
    @Deprecated
    public Map<String, String> getStatesMap() {
        return statesMap;
    }

    /**
     * Returns the feature vector, i.e. a mapping of a screen state
     * to the value 0 (unvisited) or 1 (visited).
     *
     * @return Returns the feature vector.
     */
    @Deprecated
    public Map<String, Integer> getFeatureVector() {
        return featureVector;
    }

    /**
     * Updates the feature vector. Assign to each screen state defined
     * by the given gui model either the value 0 (unvisited) or 1 (visited).
     *
     * @param guiModel A gui model wrapping screen states.
     */
    @Deprecated
    public void updateFeatureVector(IGUIModel guiModel) {
        List<IScreenState> guiStates = guiModel.getStates();
        for(IScreenState state : guiStates){
            if(this.visitedStates.contains(state.getId())){
                featureVector.put(state.getId(),1);
            } else {
                featureVector.put(state.getId(),0);
            }
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

        int finalSize = testCase.eventSequence.size();

        if (testCase.desiredSize.hasValue()) {
            finalSize = testCase.desiredSize.getValue();
        }

        try {
            int count = 0;
            for (Action action0 : testCase.eventSequence) {
                if (count < finalSize) {
                    if (!(action0 instanceof WidgetAction) || Registry.getUiAbstractionLayer().getExecutableActions().contains(action0)) {
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
                if (Properties.WIDGET_BASED_ACTIONS()) {
                    action = Randomness.randomElement(Registry.getUiAbstractionLayer().getExecutableActions());
                } else {
                    action = PrimitiveAction.randomAction();
                }
                if (!resultingTc.updateTestCase(action, count)) {
                    return resultingTc;
                }
            }

            return resultingTc;
        } finally {
            // serialize test case, record test case stats, etc.
            resultingTc.finish();
        }
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
        TestCase tc = new TestCase(UUID.randomUUID().toString());
        tc.updateTestCase("init");
        return tc;
    }

    /**
     * Executes the given action and updates the test case accordingly.
     *
     * @param action The action to be executed.
     * @param actionID The id of the action.
     * @return Returns {@code true} if the given action didn't cause a crash of the app
     *          or left the AUT, otherwise {@code false} is returned.
     */
    public boolean updateTestCase(Action action, int actionID) {
        ActionResult actionResult = updateTestCaseGetResult(action, actionID);

        switch (actionResult) {
            case SUCCESS:
            case SUCCESS_NEW_STATE:
                return true;
            case FAILURE_APP_CRASH:
            case SUCCESS_OUTBOUND:
            case FAILURE_UNKNOWN:
            case FAILURE_EMULATOR_CRASH:
                return false;
            default:
                throw new UnsupportedOperationException("Encountered an unknown action result. Cannot continue.");
        }
    }

    public ActionResult updateTestCaseGetResult(Action action, int actionID) {
        if (action instanceof WidgetAction
                && !Registry.getUiAbstractionLayer().getExecutableActions().contains(action)) {
            throw new IllegalStateException("Action not applicable to current state!");
        }

        String activityBeforeAction = Registry.getUiAbstractionLayer().getLastScreenState().getActivityName();
        MATE.log("executing action " + actionID + ": " + action);

        addEvent(action);
        ActionResult actionResult = Registry.getUiAbstractionLayer().executeAction(action);

        // track the activity transitions of each action
        String activityAfterAction = Registry.getUiAbstractionLayer().getLastScreenState().getActivityName();

        if (actionID == 0) {
            activitySequence.add(activityBeforeAction);
            activitySequence.add(activityAfterAction);
        } else {
            activitySequence.add(activityAfterAction);
        }

        MATE.log("executed action " + actionID + ": " + action);
        MATE.log("Activity Transition for action " + actionID
                + ":" + activityBeforeAction + "->" + activityAfterAction);

        switch (actionResult) {
            case SUCCESS:
            case SUCCESS_NEW_STATE:
                updateTestCase(String.valueOf(actionID));
                break;
            case FAILURE_APP_CRASH:
                setCrashDetected();
                if (Properties.RECORD_STACK_TRACE()) {
                    crashStackTrace = Registry.getEnvironmentManager().getLastCrashStackTrace();
                }
                break;
            case SUCCESS_OUTBOUND:
            case FAILURE_UNKNOWN:
            case FAILURE_EMULATOR_CRASH:
                break;
            default:
                throw new UnsupportedOperationException("Encountered an unknown action result. Cannot continue.");
        }
        return actionResult;
    }

    /**
     * Updates the test case with the given event.
     *
     * @param event A new event, e.g. the action id.
     */
    private void updateTestCase(String event) {
        IScreenState currentScreenstate = Registry.getUiAbstractionLayer().getLastScreenState();

        updateVisitedStates(currentScreenstate);
        updateVisitedActivities(currentScreenstate.getActivityName());
        updateStatesMap(currentScreenstate.getId(), event);
    }
}
