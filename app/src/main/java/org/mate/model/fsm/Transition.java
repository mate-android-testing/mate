package org.mate.model.fsm;

import android.support.annotation.NonNull;

import org.mate.interaction.action.Action;

import java.util.Objects;

public class Transition {

    protected final State source;
    protected final State target;
    protected final Action action;

    public Transition(State source, State target, Action action) {
        this.source = source;
        this.target = target;
        this.action = action;
    }

    public State getSource() {
        return source;
    }

    public State getTarget() {
        return target;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            Transition other = (Transition) o;
            return this.source.equals(other.source) && this.target.equals(other.target)
                    && this.action.equals(other.action);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, action);
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + source + ", " + action.toShortString() + ", " + target + ")";
    }
}

