package org.mate.model.fsm;

import android.support.annotation.NonNull;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;

import java.util.HashSet;
import java.util.Set;

public class FSM {

    private final State root;
    private Set<State> states;
    private Set<Transition> transitions;
    private int nextStateId;

    public FSM(State root) {
        this.root = root;
        nextStateId = 1;
        states = new HashSet<>();
        transitions = new HashSet<>();
        states.add(root);
    }

    public void addTransition(State source, State target, Action action) {

        if (!states.contains(source)) {
            throw new IllegalArgumentException("Source state not contained in FSM!");
        } else if (!states.contains(target)) {
            throw new IllegalArgumentException("Target state not contained in FSM!");
        }

        Transition transition = new Transition(source, target, action);
        transitions.add(transition);
        MATE.log_debug(String.valueOf(this));
    }

    public State getState(IScreenState screenState) {

        for (State state : states) {
            if (state.getScreenState().equals(screenState)) {
                return state;
            }
        }

        State newState = new State(nextStateId++, screenState);
        states.add(newState);
        return newState;
    }

    public boolean containsState(IScreenState screenState) {

        for (State state : states) {
            if (state.getScreenState().equals(screenState)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Root: " + root + System.lineSeparator());
        for (Transition transition : transitions) {
            builder.append(transition).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
