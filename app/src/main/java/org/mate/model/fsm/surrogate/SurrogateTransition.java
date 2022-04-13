package org.mate.model.fsm.surrogate;

import android.support.annotation.NonNull;

import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;

import java.util.Objects;
import java.util.Set;

/**
 * Describes a transition in the {@link org.mate.model.fsm.FSM}. Stores in addition the traces
 * associated with the action and the action result.
 */
public class SurrogateTransition extends Transition {

    /**
     * The traces associated with the execution of the action.
     */
    private final Set<String> traces;

    /**
     * The action result associated with the execution of the action.
     */
    private final ActionResult actionResult;

    // TODO: necessary for what?
    private int transitionCounter;

    /**
     * Creates a new transition from a given state to a target state with a given action. Each
     * transition is additionally associated with a set of traces and the action result.
     *
     * @param source The source state.
     * @param target The target state.
     * @param action The action which leads to the target state.
     * @param actionResult The result of the action.
     * @param traces The set of traces associated with the execution of the given action.
     */
    public SurrogateTransition(State source, State target, Action action, ActionResult actionResult,
                               Set<String> traces) {
        super(source, target, action);
        this.actionResult = actionResult;
        this.traces = traces;
        transitionCounter = 1;
    }

    State getSource() {
        return source;
    }

    State getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            SurrogateTransition other = (SurrogateTransition) o;
            return source.equals(other.source)
                    && target.equals(other.target)
                    && action.equals(other.action)
                    && this.traces == other.traces;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, action, this.traces);
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + source + ", " + action.toShortString() + ", "
                + target + ", " + traces + ")";
    }

    ActionResult getActionResult() {
        return actionResult;
    }

    void setTraces(Set<String> traces) {
        this.traces.addAll(traces);
    }

    Set<String> getTraces() {
        return traces;
    }

    void increaseCounter() {
        transitionCounter++;
    }

    int getCounter() {
        return transitionCounter;
    }
}

