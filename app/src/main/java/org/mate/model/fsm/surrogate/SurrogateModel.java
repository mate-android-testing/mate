package org.mate.model.fsm.surrogate;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
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
    }

    /**
     * Resets the surrogate model. This should be called at the end of each test case.
     */
    public void reset() {

        // TODO: Can we assume that a new test case always starts in the same (root) state?
        fsm.goToState(fsm.getRootState());
        checkPointState = fsm.getCurrentState();

        currentTraces.clear();
        predictedTraces.clear();
        predictedActions.clear();

        numberOfNonPredictedActions = 0;
        numberOfPredictedActions = 0;
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

        // TODO: Is this really correct?
        if (!from.equals(checkPointState)) {
            MATE.log_acc("Something fishy here...");
        }
        
        Set<Transition> transitions = fsm.getOutgoingTransitions(from, action);

        SurrogateTransition matchingTransition = null;

        // re-use the first matching transition
        for (Transition transition : transitions) {
            SurrogateTransition surrogateTransition = (SurrogateTransition) transition;
            if (surrogateTransition.getSource().equals(from)
                    && surrogateTransition.getTarget().equals(to)
                    && surrogateTransition.getTraces().equals(traces)) {
                matchingTransition = surrogateTransition;
                break;
            }
        }

        if (matchingTransition == null) {
            // create a new transition if it doesn't exist yet
            matchingTransition = new SurrogateTransition(from, to, action, actionResult, traces);
        }

        fsm.addTransition(matchingTransition);
        currentTraces.addAll(traces);
        checkPointState = fsm.getCurrentState();
    }

    /**
     * Tries to predict the given action.
     *
     * @param action The action that should be predicted.
     * @return Returns the action result associated with the given action or {@code null} if the
     *      action couldn't be predicted.
     */
    public ActionResult predictAction(Action action) {

        State currentState = fsm.getCurrentState();
        Set<Transition> transitions = fsm.getOutgoingTransitions(currentState, action);

        if (transitions.isEmpty()) {
            // can't predict action -> reset
            MATE.log_acc("Can't predict action: " + action + " in FSM state: " + currentState);
            predictedTraces.clear();
            numberOfNonPredictedActions++;
            return null;
        } else {
            MATE.log_acc("Can predict action: " + action + " in FSM state: " + currentState);
            numberOfPredictedActions++;

            // TODO: This seems to be the logic for updating the transition counter (how often it was taken)
            //  and selecting the (last) transition exceeding some pre-defined threshold. If no transition
            //  fulfills the criteria, no prediction was performed.

            /*int total_updates = 0;
            for(SurrogateTransition trans : transitions) {
                total_updates += trans.getCounter();
            }
            SurrogateTransition transition = null;
            for(SurrogateTransition trans : transitions) {
                if(trans.getCounter()/total_updates >= TRANSITION_THRESHOLD) {
                    transition = trans;
                }
            }
            if(transition == null) {
                executedActions = true;
                predictedTraces.clear();
                return null;
            }*/

            MATE.log_acc("Matching transitions: " + transitions.size());

            // TODO: Does it matter which transition we take?
            SurrogateTransition transition = (SurrogateTransition) Randomness.randomElement(transitions);

            // update predicted traces so far
            predictedTraces.addAll(transition.getTraces());

            fsm.goToState(transition.getTarget());
            return transition.getActionResult();
        }
    }

    /**
     * Moves the surrogate model back to the last check point.
     */
    public void goToLastCheckPointState() {
        fsm.goToState(checkPointState);
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
     *          prediction mode.
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
     *          {@code false} is returned.
     */
    public boolean hasPredictedEveryAction() {
        return numberOfNonPredictedActions == 0;
    }

    /**
     * Returns the number of predicted actions of the last test case.
     *
     * @return Returns the number of predicted actions.
     */
    public int getNumberOfPredictedActions() {
        return numberOfPredictedActions;
    }
}
