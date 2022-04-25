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

public class SurrogateModel extends FSMModel {

    private final List<Action> predictedActions;

    private boolean inPrediction = true;
    private boolean predictedEveryAction = false;
    private boolean executedAction = false;

    private final Set<String> currentTraces;
    private final Set<String> predictedTraces;


    private State checkPointState;

    private static final double TRANSITION_THRESHOLD = 0.8;

    private int notPredictedActions = 0;

    private int numberOfPredictedActions = 0;

    private boolean executedActions = false;

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

    public void update(IScreenState source, IScreenState target, Action action,
                       ActionResult actionResult, Set<String> traces) {

        State from = fsm.getState(source);
        State to = fsm.getState(target);

        // TODO: Is this really correct?
        if (!from.equals(checkPointState)) {
            MATE.log_acc("Something fishy here...");
        }

        State currentState = fsm.getCurrentState();
        Set<Transition> transitions = fsm.getOutgoingTransitions(currentState, action);

        SurrogateTransition matchingTransition = null;

        // take the first matching transition
        for (Transition transition : transitions) {
            SurrogateTransition surrogateTransition = (SurrogateTransition) transition;
            if (surrogateTransition.getSource().equals(from)
                    && surrogateTransition.getTarget().equals(to)
                    && surrogateTransition.getTraces().equals(traces)) {
                matchingTransition = surrogateTransition;
                matchingTransition.increaseCounter();
                break;
            }
        }

        // TODO: What was the intention here?
        if (matchingTransition == null) {
            matchingTransition = new SurrogateTransition(from, to, action, actionResult, traces);
            //notPredictedActions++;
        } else {
            // Check threshold maybe?
            //predictedActions++;
        }

        // update FSM
        fsm.addTransition(matchingTransition);

        currentTraces.addAll(traces);
        checkPointState = fsm.getCurrentState();
    }

    public ActionResult canPredictAction(Action action) {

        State currentState = fsm.getCurrentState();
        Set<Transition> transitions = fsm.getOutgoingTransitions(currentState, action);

        if (transitions.isEmpty()) {
            // can't predict action -> reset
            MATE.log_acc("Can't predict action: " + action + " in FSM state: " + currentState);
            executedActions = true;
            predictedTraces.clear();
            notPredictedActions++;
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

            // TODO: Is this 'external' move really necessary or can we do this within update()?
            fsm.goToState(transition.getTarget());

            return transition.getActionResult();
        }
    }

    /**
     * Resets the pointer of the current state where the prediction is made.
     * Resets the traces list for computing the traces associated with the last transition.
     */
    public void reset() {

        executedActions = false;

        // reset state
        fsm.goToState(fsm.getRootState());
        checkPointState = fsm.getCurrentState();

        currentTraces.clear();
        predictedTraces.clear();

        notPredictedActions = 0;
        numberOfPredictedActions = 0;

        resetPredictedActions();
    }

    public void goToLastCheckPointState() {
        predictedTraces.clear();
        fsm.goToState(checkPointState);
    }

    /**
     * Gets a set of the traces of the current test case that is predicted/modeled.
     *
     * @return The set with the traces.
     */
    public Set<String> getCurrentTraces() {
        Set<String> allTraces = new HashSet<>(predictedTraces);
        allTraces.addAll(currentTraces);
        allTraces.addAll(predictedTraces);
        return allTraces;
    }

    /**
     * Checks whether the model is at the root state.
     *
     * @return True if the current state is the root state.
     */
    public boolean initialState() {
        return fsm.getCurrentState().equals(fsm.getRootState());
    }

    public int notPredictedActions() {
        return this.notPredictedActions;
    }

    public boolean executedActions() {
        return this.executedActions;
    }

    public boolean isInPrediction() {
        return inPrediction;
    }

    public void setInPrediction(boolean inPrediction) {
        this.inPrediction = inPrediction;
    }

    public void addPredictedAction(Action action) {
        predictedActions.add(action);
    }

    public List<Action> getPredictedActions() {
        return Collections.unmodifiableList(predictedActions);
    }

    public void resetPredictedActions() {
        predictedActions.clear();
    }

    public void setExecutedAction(boolean executedAction) {
        this.executedAction = executedAction;
    }

    public boolean hasExecutedAction() {
        return executedAction;
    }

    public void setPredictedEveryAction(boolean predictedEveryAction) {
        this.predictedEveryAction = predictedEveryAction;
    }

    public boolean hasPredictedEveryAction() {
        return predictedEveryAction;
    }
}
