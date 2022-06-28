package org.mate.model.fsm.surrogate;

import org.mate.Registry;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ActionResult;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Randomness;
import org.mate.model.TestCase;
import org.mate.model.fsm.FSMModel;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;
import org.mate.state.IScreenState;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final List<Action> predictedActions = new ArrayList<>();

    /**
     * Whether we are currently in prediction mode or not.
     */
    private boolean inPrediction = true;

    /**
     * Assigns every trace a unique index.
     */
    private final Map<String, Integer> traceIndices = new HashMap<>();

    /**
     * By providing a trace index, see {@link #traceIndices}, we can look up the corresponding trace.
     */
    private final List<String> traces = new ArrayList<>();

    /**
     * The traces of actions that have been executed so far, represented by a bit set that stores
     * the indices of the traces.
     */
    private final BitSet executedTraces = new BitSet();

    /**
     * The traces of actions that have been predicted so far, represented by a bit set that stores
     * the indices of the traces.
     */
    private final BitSet predictedTraces = new BitSet();

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
    private final List<SurrogateTransition> predictedTransitions = new ArrayList<>();

    /**
     * Stores the list of executed transitions; this is necessary to construct the activity, state
     * and action sequence after a test case is complete.
     */
    private final List<SurrogateTransition> executedTransitions = new ArrayList<>();

    /**
     * Whether the last test case could be predicted or not.
     */
    private boolean predictedLastTestCase = false;

    /**
     * Creates a new surrogate model with an initial root state in underlying FSM.
     *
     * @param rootState The root state of the FSM.
     * @param packageName The package name of the AUT.
     */
    public SurrogateModel(IScreenState rootState, String packageName) {
        super(rootState, packageName);
        checkPointState = fsm.getCurrentState();
    }

    /**
     * Resets the surrogate model. This should be called after each test case or more specifically
     * after {@link org.mate.interaction.UIAbstractionLayer#storeTraces(Set)}.
     */
    private void reset() {

        executedTraces.clear();
        predictedTraces.clear();
        predictedActions.clear();

        numberOfNonPredictedActions = 0;
        numberOfPredictedActions = 0;

        /*
         * If during the execution of the cached actions an exception occurs, the surrogate model
         * would never return in prediction mode.
         */
        inPrediction = true;
    }

    /**
     * Moves the underlying FSM in the given state.
     *
     * @param screenState The current screen state.
     */
    public void goToState(IScreenState screenState) {
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

        // map each trace to its index
        final BitSet traceIndices = indexTraces(traces);

        State from = fsm.getState(source);
        State to = fsm.getState(target);

        if (!from.equals(checkPointState)) {
            MATELog.log_warn("Surrogate model not in expected state!");
            MATELog.log_warn("From state: " + from);
            MATELog.log_warn("Check point state: " + checkPointState);
            MATELog.log_warn("Current FSM state: " + fsm.getCurrentState());
        }

        Set<Transition> transitions = fsm.getOutgoingTransitions(from, action);

        SurrogateTransition matchingTransition = null;

        // re-use the first matching transition
        for (Transition transition : transitions) {
            SurrogateTransition surrogateTransition = (SurrogateTransition) transition;
            if (surrogateTransition.getTarget().equals(to)
                    && surrogateTransition.getTraces().equals(traceIndices)) {
                matchingTransition = surrogateTransition;
                matchingTransition.increaseFrequencyCounter();
                break;
            }
        }

        if (matchingTransition == null) {
            // create a new transition if it doesn't exist yet
            matchingTransition = new SurrogateTransition(from, to, action, actionResult, traceIndices);
        }

        executedTransitions.add(matchingTransition);
        fsm.addTransition(matchingTransition);
        addTraces(executedTraces, traceIndices);
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

            addTraces(predictedTraces, transition.getTraces());
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
     * Retrieves the set of traces that have been associated with the last test case.
     *
     * @return Returns the set of collected traces.
     */
    private Set<String> getTraces() {

        BitSet allTraces = new BitSet(Math.max(predictedTraces.size(), executedTraces.size()));
        addTraces(allTraces, predictedTraces);
        addTraces(allTraces, executedTraces);

        Set<String> traces = new HashSet<>();

        // get the indices of all set bits and look up the corresponding traces
        for (int i = allTraces.nextSetBit(0); i >= 0; i = allTraces.nextSetBit(i + 1)) {
            traces.add(this.traces.get(i));
        }

        return traces;
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
    private boolean hasPredictedEveryAction() {
        /*
         * The second condition is mandatory in order to distinguish the initial state of the
         * surrogate model from any other state. Without this condition, the very first restart of
         * the AUT wouldn't be executed for instance.
         */
        return numberOfNonPredictedActions == 0 && numberOfPredictedActions > 0;
    }

    /**
     * Whether the last test case could be predicted or not.
     *
     * @return Returns {@code true} if the last test case could be predicted, otherwise {@code false}
     *         is returned.
     */
    public boolean hasPredictedLastTestCase() {
        return predictedLastTestCase;
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
            MATELog.log("executing action " + actionID + ": " + action);
            MATELog.log("executed action " + actionID + ": " + action);
            MATELog.log("Activity Transition for action " + actionID
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
        MATELog.log("Predicted actions: " + getNumberOfPredictedActions());
        MATELog.log("Non-predicted actions: " + getNumberOfNonPredictedActions());

        if (hasPredictedEveryAction()) {
            MATELog.log("Predicted every action!");
            predictedLastTestCase = true;
        } else {
            predictedLastTestCase = false;
        }

        /*
         * We need to store both files traces.txt and info.txt on the external storage such
         * that the subsequent calls of the FitnessUtils and CoverageUtils class work properly.
         * However, those calls will send again a broadcast to the tracer, which in turn
         * overwrites the info.txt with a value not matching the actual number of traces.
         * This only works because MATE-Server doesn't enforce equality between these numbers,
         * but we should be aware of this issue.
         */
        Registry.getUiAbstractionLayer().storeTraces(getTraces());

        // reset the surrogate model
        reset();
    }

    /**
     * Maps a trace to its index.
     *
     * @param trace The trace to be indexed.
     * @return Returns the index of the trace in the {@link #traceIndices} map.
     */
    private Integer indexTrace(final String trace) {

        assert traceIndices.size() == traces.size();

        final Integer newIndex = traceIndices.size();
        final Integer oldIndex = traceIndices.putIfAbsent(trace, newIndex);

        if (oldIndex == null) {
            // we have a new trace
            traces.add(trace);
            return newIndex;
        } else {
            return oldIndex;
        }
    }

    /**
     * Maps multiple traces to its indices.
     *
     * @param traces The traces to be indexed.
     * @return Returns the indices of the traces in the {@link #traceIndices} map.
     */
    private BitSet indexTraces(final Collection<String> traces) {
        final BitSet traceIndices = new BitSet();
        traces.stream().map(this::indexTrace).forEach(traceIndices::set);
        return traceIndices;
    }

    /**
     * Adds (unions) the source traces to the target traces. At the bit level, this is simple a
     * logical or operation.
     *
     * @param target The target traces.
     * @param source The source traces.
     */
    private void addTraces(final BitSet target, final BitSet source) {
        target.or(source);
    }
}
