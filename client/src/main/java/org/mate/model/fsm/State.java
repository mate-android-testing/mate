package org.mate.model.fsm;

import androidx.annotation.NonNull;

import org.mate.state.IScreenState;

import java.util.Objects;

public class State {

    private final int id;
    private final IScreenState screenState;

    State(int id, IScreenState screenState) {
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
        String result = "S" + id;
        if(screenState != null) {
            result += " [" + screenState.getActivityName() + "]";
        }
        return result;
    }
}
