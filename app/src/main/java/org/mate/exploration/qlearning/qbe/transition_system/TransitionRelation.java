package org.mate.exploration.qlearning.qbe.transition_system;

import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.exploration.qlearning.qbe.abstractions.state.State;

import java.util.Objects;

import static org.mate.interaction.UIAbstractionLayer.ActionResult;

/**
 * Defines a transition in the Extended Labeled Transition System (ELTS), i.e. a connection from
 * state v to state v' with action a.
 *
 * @param <S> The generic state type.
 * @param <A> The generic action type.
 */
public final class TransitionRelation<S extends State<A>, A extends Action> {

    /**
     * The source state.
     */
    public final S from;

    /**
     * The action that triggers the transition.
     */
    public final A trigger;

    /**
     * The target state.
     */
    public final S to;

    /**
     * The action result, i.e. success or failure.
     */
    public final ActionResult actionResult;

    /**
     * Initialises a new transition.
     *
     * @param from The source state of the transition.
     * @param trigger The action leading from the source to the target state.
     * @param to The target state of the transition.
     * @param actionResult The action result.
     */
    public TransitionRelation(final S from, final A trigger, final S to, final ActionResult actionResult) {
        this.from = Objects.requireNonNull(from);
        this.trigger = Objects.requireNonNull(trigger);
        this.to = to;
        this.actionResult = Objects.requireNonNull(actionResult);
    }

    /**
     * A copy constructor.
     *
     * @param ts The transition that should be cloned.
     */
    public TransitionRelation(final TransitionRelation<S, A> ts) {
        this(ts.from, ts.trigger, ts.to, ts.actionResult);
    }

    /**
     * Checks for equality between two transitions. Two transitions are considered equal if they
     * have the same source and target state as well as the same action.
     *
     * @param o The other transition.
     * @return Returns {@code true} if the two transitions are equal, otherwise {@code false} is
     *          returned.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final TransitionRelation<?, ?> other = (TransitionRelation<?, ?>) o;
            return from.equals(other.from)
                    && trigger.equals(other.trigger)
                    && Objects.equals(to, other.to);
        }
    }

    /**
     * Computes the hash code for the given transition.
     *
     * @return Returns the hash code associated with the given transition.
     */
    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + trigger.hashCode();
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }

    /**
     * Provides a custom string representation for the transition.
     *
     * @return Returns the string representation of the given transition.
     */
    @Override
    public String toString() {
        return "{\"from\":" + from + ",\"trigger\":" + trigger + ",\"to\":" + to
                + ",\"actionResult\":\"" + actionResult + "\"}";
    }
}
