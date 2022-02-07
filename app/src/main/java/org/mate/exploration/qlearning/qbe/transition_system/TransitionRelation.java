package org.mate.exploration.qlearning.qbe.transition_system;

import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.exploration.qlearning.qbe.abstractions.state.State;

import java.util.Objects;

import static org.mate.interaction.UIAbstractionLayer.ActionResult;

public final class TransitionRelation<S extends State<A>, A extends Action> {

    public final S from;
    public final A trigger;
    public final S to;
    public final ActionResult actionResult;

    public TransitionRelation(final S from, final A trigger, final S to, final ActionResult actionResult) {
        this.from = Objects.requireNonNull(from);
        this.trigger = Objects.requireNonNull(trigger);
        this.to = to;
        this.actionResult = Objects.requireNonNull(actionResult);
    }

    public TransitionRelation(final TransitionRelation<S, A> ts) {
        this(ts.from, ts.trigger, ts.to, ts.actionResult);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final TransitionRelation<?, ?> other = (TransitionRelation<?, ?>) o;
            return from.equals(other.from) && trigger.equals(other.trigger) && Objects.equals(to,
                    other.to);
        }
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + trigger.hashCode();
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{\"from\":" + from + ",\"trigger\":" + trigger + ",\"to\":" + to + ",\"actionResult\":\"" + actionResult + "\"}";
    }
}
