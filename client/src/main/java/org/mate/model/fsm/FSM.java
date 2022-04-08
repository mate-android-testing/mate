package org.mate.model.fsm;

import androidx.annotation.NonNull;

import org.mate.Properties;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.utils.MATELog;
import org.mate.state.IScreenState;
import org.mate.state.equivalence.IStateEquivalence;
import org.mate.state.equivalence.StateEquivalenceFactory;
import org.mate.state.equivalence.StateEquivalenceLevel;

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

/**
 * A simple finite state machine for the representing the {@link org.mate.model.IGUIModel}.
 */
public class FSM {

    /**
     * The (virtual) root state.
     */
    private final State root;

    /**
     * The states of the FSM.
     */
    private final Set<State> states;

    /**
     * The transitions of the FSM.
     */
    private final Set<Transition> transitions;

    /**
     * The id of the next new state.
     */
    private int nextStateId;

    /**
     * Whether we reached a new state with the last transition.
     */
    private boolean reachedNewState;

    /**
     * The package name of the AUT.
     */
    private final String packageName;

    /**
     * The current state in the FSM.
     */
    private State currentState;

    /**
     * The current state equivalence level that defines how two {@link State}s are compared for
     * equality. Depending on the state equivalence level, the FSM may contain more or less states.
     */
    private static final StateEquivalenceLevel STATE_EQUIVALENCE_LEVEL
            = Properties.STATE_EQUIVALENCE_LEVEL();

    /**
     * Creates a new finite state machine with an initial start state.
     *
     * @param root The start or root state.
     */
    public FSM(State root, String packageName) {
        this.root = root;
        this.packageName = packageName;
        nextStateId = 1;
        states = new HashSet<>();
        transitions = new HashSet<>();
        states.add(root);

        // the initial state is a new state
        reachedNewState = true;
        currentState = root;
    }

    /**
     * Adds a new transition to the FSM linking two states with an action. As a side effect tracks
     * whether we reached a new state and internally updates the current FSM state.
     *
     * @param transition The new transition.
     */
    public void addTransition(Transition transition) {

        states.add(transition.getSource());

        // check whether we reached a new state
        reachedNewState = states.add(transition.getTarget());

        if (transitions.add(transition)) {
            MATELog.log_debug(String.valueOf(this));
        }

        currentState = transition.getTarget();
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
     * Returns the transitions in the FSM.
     *
     * @return Returns the transitions in the FSM.
     */
    public Set<Transition> getTransitions() {
        return Collections.unmodifiableSet(transitions);
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

        IStateEquivalence stateEquivalence
                = StateEquivalenceFactory.getStateEquivalenceCheck(STATE_EQUIVALENCE_LEVEL);

        for (State state : states) {
            if (state != root) { // skip the virtual root state
                if (stateEquivalence.checkEquivalence(screenState, state.getScreenState())) {
                    return state;
                }
            }
        }

        return new State(nextStateId++, screenState);
    }

    /**
     * Whether the last transition lead to a new state.
     *
     * @return Returns {@code} if a new state has been reached,
     *         otherwise {@code} false is returned.
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
    public Set<Transition> getOutgoingTransitions(State source) {
        return transitions.stream()
                .filter(transition -> transition.getSource().equals(source))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the outgoing transitions from the given source state with the given action.
     *
     * @param source The source state.
     * @param action The given action.
     * @return Returns the outgoing transitions from the given state and action.
     */
    public Set<Transition> getOutgoingTransitions(State source, Action action) {
        return transitions.stream()
                .filter(transition -> transition.getSource().equals(source))
                .filter(transition -> transition.getAction().equals(action))
                .collect(Collectors.toSet());
    }

    /**
     * Returns an arbitrary transition between the given source and target state.
     *
     * @param source The source state.
     * @param target The target state.
     * @return Returns any transition between the given source and target state if such transition
     *         exists.
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
     * Returns the (virtual) root state of the FSM.
     *
     * @return Returns the root state.
     */
    public State getRootState() {
        return root;
    }

    /**
     * Returns the activity predecessors of the given activity.
     *
     * @param activity The given activity.
     * @return Returns the activities that have a direct transition to the given activity.
     */
    public Set<String> getActivityPredecessors(String activity) {

        Set<String> activityPredecessors = new HashSet<>();

        // find all transitions that lead to the given activity and represent an activity of the AUT
        for (Transition transition : transitions) {
            if (transition.getTarget().getScreenState().getActivityName().equals(activity)) {
                // check that the source state represents a different activity of the AUT
                IScreenState sourceState = transition.getSource().getScreenState();
                if (sourceState.getPackageName().equals(packageName)
                        && !sourceState.getActivityName().equals(activity)) {
                    activityPredecessors.add(sourceState.getActivityName());
                }
            }
        }

        return activityPredecessors;
    }

    /**
     * Returns the state the FSM is in right now.
     *
     * @return Returns the current state.
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Returns the id of the current FSM state.
     *
     * @return Returns the current state id.
     */
    public String getCurrentStateId() {
        return currentState.getScreenState().getId();
    }

    /**
     * Moves the FSM in the given state.
     *
     * @param state The new state to which the FSM should move.
     */
    public void goToState(State state) {

        // TODO: This may happen if the start screen state is not deterministic!
        if (!states.contains(state)) {
            throw new IllegalStateException("Can't move FSM in a state that it has not seen yet!");
        }

        currentState = state;
    }

    /**
     * Returns a simple text representation of the FSM.
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
