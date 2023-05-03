package org.mate.model.fsm;

import android.support.annotation.NonNull;

import org.mate.state.IScreenState;

import java.util.Objects;

public class State {

    protected final int id;
    protected final IScreenState screenState;

    public State(int id, IScreenState screenState) {
        this.id = id;
        this.screenState = screenState;
    }

    public int getId() {
        return id;
    }

    public IScreenState getScreenState() {
        return screenState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            State other = (State) o;
            return this.id == other.id;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @NonNull
    @Override
    public String toString() {
        return "S" + id + " [" + screenState.getActivityName() + "]";
    }
}
