package org.mate.model.fsm;

import android.support.annotation.NonNull;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FSM {

    private final State root;
    private final Set<State> states;
    private final Set<Transition> transitions;
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
     * Tries to find the shortest path between the given states.
     *
     * @param from The source state.
     * @param to The target state.
     * @return Returns the shortest path between the given states if such path exists.
     */
    public Optional<List<Transition>> shortestPath(State from, State to) {

        // bfs traversal
        Deque<State> workQueue = new LinkedList<>();
        Set<State> exploredStates = new HashSet<>();
        Map<State, State> predecessors = new HashMap<>();

        exploredStates.add(from);
        workQueue.add(from);

        while (!workQueue.isEmpty()) {

            State state = workQueue.poll();

            if (Objects.equals(state, to)) {
                return Optional.of(shortestPath(from, to, predecessors));
            } else {
                for (Transition transition : getOutgoingTransitions(state)) {
                    State target = transition.getTarget();

                    if (!exploredStates.contains(target)) {
                        exploredStates.add(target);
                        workQueue.add(target);
                        predecessors.put(target, state);
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the outgoing transitions from the given source state.
     *
     * @param source The source state.
     * @return Returns the outgoing transitions from the given state.
     */
    private Set<Transition> getOutgoingTransitions(State source) {
        return transitions.stream()
                .filter(transition -> transition.getSource().equals(source))
                .collect(Collectors.toSet());
    }

    /**
     * Returns an arbitrary transition between the given source and target state.
     *
     * @param source The source state.
     * @param target The target state.
     * @return Returns any transition between the given source and target state if such transition
     *          exists.
     */
    private Optional<Transition> getAnyTransition(State source, State target) {
        return transitions.stream()
                .filter(transition -> transition.getSource().equals(source)
                        && transition.getTarget().equals(target))
                .findAny();
    }

    /**
     * Returns the shortest path between the given two states.
     *
     * @param from The source state.
     * @param to The target state.
     * @param predecessors Maintains the predecessor for each state.
     * @return Returns the shortest path between the source and target state.
     */
    private List<Transition> shortestPath(final State from, final State to,
                                          final Map<State, State> predecessors) {

        List<Transition> path = new LinkedList<>();

        State state = to;

        while (!Objects.equals(state, from)) {
            State predecessor = predecessors.get(state);
            Transition transition = getAnyTransition(predecessor, state)
                    .orElseThrow(() -> new IllegalStateException("No transition found!"));
            path.add(transition);
            state = predecessor;
        }

        Collections.reverse(path);
        return path;
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
