package org.mate.model.fsm.surrogate;

import androidx.annotation.NonNull;

import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ActionResult;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;

import java.util.BitSet;
import java.util.Objects;

/**
 * Describes a transition in the {@link org.mate.model.fsm.FSM}. Stores in addition the traces
 * associated with the action and the action result.
 */
public class SurrogateTransition extends Transition {

    /**
     * The traces associated with the execution of the action, represented by a BitSet which stores
     * the indexes of the traces.
     */
    private final BitSet traces;

    /**
     * The action result associated with the execution of the action.
     */
    private final ActionResult actionResult;

    /**
     * Tracks how often the transition was taken.
     */
    private int frequencyCounter;

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
                               BitSet traces) {
        super(source, target, action);
        this.actionResult = actionResult;
        this.traces = traces;
        frequencyCounter = 0;
    }

    /**
     * Returns the action result associated with the action leading from the source to the target
     * state.
     *
     * @return Returns the action result.
     */
    ActionResult getActionResult() {
        return actionResult;
    }

    /**
     * Returns the traces associated with the transition.
     *
     * @return Returns the set of traces associated with the transition.
     */
    BitSet getTraces() {
        return traces;
    }

    /**
     * {@inheritDoc}
     */
    State getSource() {
        return source;
    }

    /**
     * {@inheritDoc}
     */
    State getTarget() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    Action getAction() {
        return action;
    }

    /**
     * Returns the frequency counter.
     *
     * @return Returns the frequency counter.
     */
    int getFrequencyCounter() {
        return frequencyCounter;
    }

    /**
     * Increases the frequency counter.
     */
    void increaseFrequencyCounter() {
        frequencyCounter++;
    }

    /**
     * Compares two transitions for equality.
     *
     * @param o The other transition.
     * @return Returns {@code true} if the transitions are identical, otherwise {@code false} is
     *         returned.
     */
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
                    && traces.equals(other.traces);
        }
    }

    /**
     * Computes the hash code for the given transition.
     *
     * @return Returns the hash code of the transition.
     */
    @Override
    public int hashCode() {
        return Objects.hash(source, target, action, traces);
    }

    /**
     * Provides a simple textual representation of the transition.
     *
     * @return Returns the string representation of the transition.
     */
    @NonNull
    @Override
    public String toString() {
        return "(" + source + ", " + action.toShortString() + ", " + target + ", " + traces + ")";
    }
}

