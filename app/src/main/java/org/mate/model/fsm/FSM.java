package org.mate.model.fsm;

import android.support.annotation.NonNull;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FSM {

    private final State root;
    private Set<State> states;
    private Set<Transition> transitions;
    private int nextStateId;
    private boolean reachedNewState;

    /**
     * Creates a new finite state machine with an initial start state.
     *
     * @param root The start or root state.
     */
    public FSM(State root) {
        this.root = root;
        nextStateId = 1;
        states = new HashSet<>();
        transitions = new HashSet<>();
        states.add(root);

        // the initial state is a new state
        reachedNewState = true;
    }

    /**
     * Adds a new transition to the FSM linking two states with an action.
     * As a side effect tracks whether we reached a new state.
     *
     * @param source The source state.
     * @param target The target or destination state.
     * @param action The action linking both states.
     */
    public void addTransition(State source, State target, Action action) {

        states.add(source);

        // check whether we reached a new state
        reachedNewState = states.add(target);

        Transition transition = new Transition(source, target, action);
        if (transitions.add(transition)) {
            MATE.log_debug(String.valueOf(this));
        }
    }

    /**
     * Returns the transition that are labeled by the given action.
     *
     * @param action The given action.
     * @return Returns the transitions that are labeled by the given action.
     */
    public Set<Transition> getTransitions(Action action) {

        Set<Transition> transitions = new HashSet<>();
        for (Transition transition : this.transitions) {
            if (transition.getAction().equals(action)) {
                transitions.add(transition);
            }
        }
        return transitions;
    }

    /**
     * Returns the number of states in the FSM.
     *
     * @return Returns the number of states in the FSM.
     */
    public int getNumberOfStates() {
        return states.size();
    }

    /**
     * Converts the given screen state into a state used by the FSM.
     * Returns a cached version of this state or a new state if the given
     * screen state is new.
     *
     * @param screenState The given screen state.
     * @return Returns a FSM state corresponding to the given screen state.
     */
    public State getState(IScreenState screenState) {

        for (State state : states) {
            if (state.getScreenState().equals(screenState)) {
                return state;
            }
        }

        return new State(nextStateId++, screenState);
    }

    /**
     * Whether the last transition lead to a new state.
     *
     * @return Returns {@code} if a new state has been reached,
     *          otherwise {@code} false is returned.
     */
    public boolean reachedNewState() {
        return reachedNewState;
    }

    /**
     * Returns the states of the FSM.
     *
     * @return Returns the states of the FSM.
     */
    public Set<State> getStates() {
        return Collections.unmodifiableSet(states);
    }

    /**
     *  Returns a simple text representation of the FSM.
     *
     * @return Returns a simple string representation of the FSM.
     */
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
