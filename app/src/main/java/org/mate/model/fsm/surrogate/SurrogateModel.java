package org.mate.model.fsm.surrogate;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
import org.mate.model.TestCase;
import org.mate.model.fsm.FSMModel;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;
import org.mate.state.IScreenState;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wraps a surrogate model around the traditional FSM model. This enables the prediction of actions
 * and complete test cases in the best case. In general, we assume a higher throughput of actions
 * the longer the execution takes and the more actions that could be predicted.
 */
public class SurrogateModel extends FSMModel {

    /**
     * The predicted actions so far.
     */
    private final List<Action> predictedActions;

    /**
     * Whether we are currently in prediction mode or not.
     */
    private boolean inPrediction = true;

    /**
     * The set of traces that have been collected so far.
     */
    private final Set<String> currentTraces;

    /**
     * The set of traces that could be predicted so far.
     */
    private final Set<String> predictedTraces;

    /**
     * The last check point to which the surrogate model returns in case a prediction couldn't be
     * performed.
     */
    private State checkPointState;

    /**
     * The number of non-predicted actions per test case.
     */
    private int numberOfNonPredictedActions = 0;

    /**
     * The number of predicted actions per test case.
     */
    private int numberOfPredictedActions = 0;

    /**
     * Stores the list of predicted transitions; this is necessary to construct the activity, state
     * and action sequence after a test case is complete.
     */
    private final List<SurrogateTransition> predictedTransitions;

    /**
     * Stores the list of executed transitions; this is necessary to construct the activity, state
     * and action sequence after a test case is complete.
     */
    private final List<SurrogateTransition> executedTransitions;

    /**
     * Creates a new surrogate model with an initial root state in underlying FSM.
     *
     * @param rootState The root state of the FSM.
     * @param packageName The package name of the AUT.
     */
    public SurrogateModel(IScreenState rootState, String packageName) {
        super(rootState, packageName);
        currentTraces = new HashSet<>();
        predictedTraces = new HashSet<>();
        checkPointState = fsm.getCurrentState();
        predictedActions = new ArrayList<>();
        predictedTransitions = new ArrayList<>();
        executedTransitions = new ArrayList<>();
    }

    /**
     * Resets the surrogate model. This should be called at the end of each test case.
     */
    public void reset(IScreenState screenState) {

        currentTraces.clear();
        predictedTraces.clear();
        predictedActions.clear();

        numberOfNonPredictedActions = 0;
        numberOfPredictedActions = 0;

        // we need to bring the FSM in the correct state again
        goToState(screenState);
    }

    /**
     * Moves the underlying FSM in the given state.
     *
     * @param screenState The current screen state.
     */
    private void goToState(IScreenState screenState) {
        State state = fsm.getState(screenState);
        fsm.goToState(state);
        checkPointState = fsm.getCurrentState();
    }

    /**
     * Updates the surrogate model and inherently the underlying FSM with a new transition.
     *
     * @param source The source state.
     * @param target the target state.
     * @param action The action leading from the source to the target state.
     * @param actionResult The action result associated with the given action.
     * @param traces The traces produced by executing the given action.
     */
    public void update(final IScreenState source, final IScreenState target, final Action action,
                       final ActionResult actionResult, final Set<String> traces) {

        State from = fsm.getState(source);
        State to = fsm.getState(target);

        if (!from.equals(checkPointState)) {
            MATE.log_warn("Surrogate model not in expected state!");
            MATE.log_warn("From state: " + from);
            MATE.log_warn("Check point state: " + checkPointState);
            MATE.log_warn("Current FSM state: " + fsm.getCurrentState());
        }

        Set<Transition> transitions = fsm.getOutgoingTransitions(from, action);

        SurrogateTransition matchingTransition = null;

        // re-use the first matching transition
        for (Transition transition : transitions) {
            SurrogateTransition surrogateTransition = (SurrogateTransition) transition;
            if (surrogateTransition.getTarget().equals(to)
                    && surrogateTransition.getTraces().equals(traces)) {
                matchingTransition = surrogateTransition;
                matchingTransition.increaseFrequencyCounter();
                break;
            }
        }

        if (matchingTransition == null) {
            // create a new transition if it doesn't exist yet
            matchingTransition = new SurrogateTransition(from, to, action, actionResult, traces);
        }

        executedTransitions.add(matchingTransition);
        fsm.addTransition(matchingTransition);
        currentTraces.addAll(traces);
        checkPointState = fsm.getCurrentState();
    }

    /**
     * Tries to predict the given action.
     *
     * @param action The action that should be predicted.
     * @return Returns the action result associated with the given action or {@code null} if the
     *         action couldn't be predicted.
     */
    public ActionResult predictAction(Action action) {

        State currentState = fsm.getCurrentState();
        Set<Transition> transitions = fsm.getOutgoingTransitions(currentState, action);

        if (transitions.isEmpty()) {
            // can't predict action -> reset
            predictedTraces.clear();
            numberOfNonPredictedActions++;
            predictedTransitions.clear();
            return null;
        } else {
            numberOfPredictedActions++;

            // pick the transition with the highest frequency counter
            final int highestCounter = transitions.stream()
                    .mapToInt(transition -> ((SurrogateTransition) transition).getFrequencyCounter())
                    .max()
                    .orElseThrow(() -> new IllegalStateException("Empty set not allowed!"));

            Set<Transition> mostVisitedTransitions = transitions.stream()
                    .filter(transition -> ((SurrogateTransition) transition).getFrequencyCounter() == highestCounter)
                    .collect(Collectors.toSet());

            SurrogateTransition transition = (SurrogateTransition) Randomness.randomElement(mostVisitedTransitions);

            predictedTraces.addAll(transition.getTraces());
            predictedTransitions.add(transition);
            fsm.goToState(transition.getTarget());
            return transition.getActionResult();
        }
    }

    /**
     * Moves the surrogate model back to the last check point.
     *
     * @return Returns the screen state backed by the current FSM state.
     */
    public IScreenState goToLastCheckPointState() {
        fsm.goToState(checkPointState);
        return checkPointState.getScreenState();
    }

    /**
     * Retrieves the set of traces that have been produced for the last test case.
     *
     * @return Returns the set of collected traces.
     */
    public Set<String> getCurrentTraces() {
        Set<String> allTraces = new HashSet<>(predictedTraces);
        allTraces.addAll(currentTraces);
        return allTraces;
    }

    /**
     * Whether the surrogate model is in prediction mode or not.
     *
     * @return Returns {@code true} if in prediction mode, otherwise {@code false} is returned.
     */
    public boolean isInPrediction() {
        return inPrediction;
    }

    /**
     * Turns on or off the prediction mode.
     *
     * @param inPrediction {@code true} to turn on prediction mode or {@code false} to turn off
     *         prediction mode.
     */
    public void setInPrediction(boolean inPrediction) {
        this.inPrediction = inPrediction;
    }

    /**
     * Adds an action to the list of predicted actions.
     *
     * @param action The predictable action.
     */
    public void addPredictedAction(Action action) {
        predictedActions.add(action);
    }

    /**
     * Returns the list of predicted actions.
     *
     * @return Returns the list of predicted actions.
     */
    public List<Action> getPredictedActions() {
        return Collections.unmodifiableList(predictedActions);
    }

    /**
     * Removes all actions from the list of predicted actions.
     */
    public void resetPredictedActions() {
        predictedActions.clear();
    }

    /**
     * Checks whether all actions of a test case could be predicted or not.
     *
     * @return Returns {@code true} if all actions of a test case could be predicted, otherwise
     *         {@code false} is returned.
     */
    public boolean hasPredictedEveryAction() {
        /*
         * The second condition is mandatory in order to distinguish the initial state of the
         * surrogate model from any other state. Without this condition, the very first restart of
         * the AUT wouldn't be executed for instance.
         */
        return numberOfNonPredictedActions == 0 && numberOfPredictedActions > 0;
    }

    /**
     * Returns the number of predicted actions of the last test case.
     *
     * @return Returns the number of predicted actions.
     */
    public int getNumberOfPredictedActions() {
        return numberOfPredictedActions;
    }

    /**
     * Returns the number of non-predicted actions of the last test case.
     *
     * @return Returns the number of non-predicted actions.
     */
    public int getNumberOfNonPredictedActions() {
        return numberOfNonPredictedActions;
    }

    /**
     * Returns the current screen state the FSM is in.
     *
     * @return Returns the current screen state.
     */
    public IScreenState getCurrentScreenState() {
        return fsm.getCurrentState().getScreenState();
    }

    /**
     * Updates the given test case's action, state and activity sequence. This is necessary since
     * a previous prediction might be wrong. This method needs to be called after a test case is
     * complete and before {@link TestCase#finish()} is called.
     *
     * @param testCase The test case that needs to be updated.
     */
    private void updateTestCaseSequences(TestCase testCase) {

        final List<Action> actionSequence = new ArrayList<>();
        final List<String> stateSequence = new ArrayList<>();
        final List<String> activitySequence = new ArrayList<>();

        // either one of both lists is empty or we need to merge both lists in the right order
        List<SurrogateTransition> transitions = new ArrayList<>(executedTransitions);
        transitions.addAll(predictedTransitions);

        int actionID = 0;

        for (SurrogateTransition transition : transitions) {

            IScreenState source = transition.getSource().getScreenState();
            IScreenState target = transition.getTarget().getScreenState();
            Action action = transition.getAction();

            actionSequence.add(action);
            stateSequence.add(target.getId());
            activitySequence.add(target.getActivityName());

            // We need to report the correct logs for the analysis framework!
            MATE.log("executing action " + actionID + ": " + action);
            MATE.log("executed action " + actionID + ": " + action);
            MATE.log("Activity Transition for action " + actionID
                    + ":" + source.getActivityName() + "->" + target.getActivityName());

            actionID++;
        }

        testCase.getActionSequence().addAll(actionSequence);
        testCase.getStateSequence().addAll(stateSequence);
        testCase.getActivitySequence().addAll(activitySequence);

        predictedTransitions.clear();
        executedTransitions.clear();
    }

    /**
     * Updates the given test case, i.e. it updates the activity, state and action sequence as well
     * as writes the collected traces to the external storage. This method needs to be called after
     * a test case has been fully constructed and before the call to {@link TestCase#finish()}.
     *
     * @param testCase The test case to be updated.
     */
    public void updateTestCase(TestCase testCase) {

        /*
         * We need to manually adjust the activity, state and action sequence of a test case,
         * since intermediate predictions by the surrogate model might be wrong, i.e. when
         * we execute cached (predicted) actions and those lead to a different state. Likewise,
         * we need to log the action sequence and activity transitions after the test case
         * is complete.
         */
        updateTestCaseSequences(testCase);

        // These logs are parsed by the analysis framework!
        MATE.log("Predicted actions: " + getNumberOfPredictedActions());
        MATE.log("Non-predicted actions: " + getNumberOfNonPredictedActions());

        if (hasPredictedEveryAction()) {
            MATE.log("Predicted every action!");
        }

        /*
         * We need to store both files traces.txt and info.txt on the external storage such
         * that the subsequent calls of the FitnessUtils and CoverageUtils class work properly.
         * However, those calls will send again a broadcast to the tracer, which in turn
         * overwrites the info.txt with a value not matching the actual number of traces.
         * This only works because MATE-Server doesn't enforce equality between these numbers,
         * but we should be aware of this issue.
         */
        Registry.getUiAbstractionLayer().storeTraces(currentTraces);
    }
}
