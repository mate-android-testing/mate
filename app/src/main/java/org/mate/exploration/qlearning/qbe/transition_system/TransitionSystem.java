package org.mate.exploration.qlearning.qbe.transition_system;

import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.exploration.qlearning.qbe.abstractions.state.State;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Defines the Extended Labeled Transition System (ELTS).
 *
 * @param <S> The generic state type.
 * @param <A> The generic action type.
 */
public final class TransitionSystem<S extends State<A>, A extends Action> {

    /**
     * The initial state v0.
     */
    private final S initialState;

    /**
     * The set of states.
     */
    private final Set<S> states = new HashSet<>();

    /**
     * The set of actions.
     */
    private final Set<A> actions = new HashSet<>();

    /**
     * The set of transitions.
     */
    private final Set<TransitionRelation<S, A>> transitions = new HashSet<>();

    /**
     * Initialises a new transition system with the given initial state.
     *
     * @param initialState The initial state v0.
     */
    public TransitionSystem(final S initialState) {
        this.initialState = Objects.requireNonNull(initialState);
        states.add(initialState);
        actions.addAll(initialState.getActions());
    }

    /**
     * Returns the initial state v0.
     *
     * @return Returns the initial state.
     */
    public S getInitialState() {
        return initialState;
    }

    /**
     * Returns the states in the given transition system.
     *
     * @return Returns the states.
     */
    public Set<S> getStates() {
        return Collections.unmodifiableSet(states);
    }

    /**
     * Returns the actions in the given transition system.
     *
     * @return Returns the actions.
     */
    public Set<A> getActions() {
        return Collections.unmodifiableSet(actions);
    }

    /**
     * Returns the transitions in the given transition system.
     *
     * @return Returns the transitions.
     */
    public Set<TransitionRelation<S, A>> getTransitions() {
        return Collections.unmodifiableSet(transitions);
    }

    /**
     * Adds a new transition to the ELTS.
     *
     * @param transitionRelation The new transition.
     * @return Returns {@code true} if the new transition is non-deterministic, otherwise
     *          {@code false} is returned.
     */
    public boolean addTransition(final TransitionRelation<S, A> transitionRelation) {

        states.add(transitionRelation.from);
        actions.addAll(transitionRelation.from.getActions());
        actions.add(transitionRelation.trigger);

        if (transitionRelation.to != null) {
            states.add(transitionRelation.to);
            actions.addAll(transitionRelation.to.getActions());
        }

        final boolean nonDeterministicTransitionRelation = transitions.stream().anyMatch(
                t -> t.from.equals(transitionRelation.from) && t.trigger.equals(transitionRelation.trigger)
                        && !Objects.equals(t.to, transitionRelation.to));
        transitions.add(transitionRelation);
        return nonDeterministicTransitionRelation;
    }

    /**
     * Removes the given transition from the ELTS.
     *
     * @param transitionRelation The transition that should be removed.
     */
    public void removeTransition(final TransitionRelation<S, A> transitionRelation) {
        transitions.remove(transitionRelation);
    }

    /**
     * Adds a set of actions to the ELTS.
     *
     * @param actions The set of actions that should be added.
     */
    public void addActions(final Collection<A> actions) {
        this.actions.addAll(actions);
    }

    /**
     * Retrieves the actions that are applicable in the given state.
     *
     * @param state The given state.
     * @return Returns the applicable actions in the given state.
     */
    @SuppressWarnings("unused")
    public Set<A> nextActions(final S state) {
        return transitions.stream().filter(transitions -> transitions.from.equals(state))
                .map(transition -> transition.trigger).collect(toSet());
    }

    /**
     * Retrieves the transitions that define the given source state and the given action.
     *
     * @param state The source state.
     * @param action The action.
     * @return Returns the transitions that match the given source state and action.
     */
    public Set<TransitionRelation<S, A>> nextStates(final S state, final A action) {
        return transitions.stream()
                .filter(transition -> transition.from.equals(state) && transition.trigger.equals(action))
                .collect(toSet());
    }

    /**
     * Removes the unreachable states.
     */
    public void removeUnreachableStates() {
        // TODO: This can probably be implemented more efficiently.
        final Set<TransitionRelation<S, A>> reachableTransitions = new HashSet<>(transitions.size());
        final Set<S> reachableStates = new HashSet<>(states.size());
        reachableStates.add(initialState);

        boolean change;
        do {
            final Set<TransitionRelation<S, A>> newTransitions = transitions.stream()
                    .filter(transitions -> reachableStates.contains(transitions.from))
                    .collect(toSet());
            final Set<S> newStates = newTransitions.stream().map(transition -> transition.to)
                    .filter(Objects::nonNull)
                    .collect(toSet());
            change = reachableTransitions.addAll(newTransitions);
            change = reachableStates.addAll(newStates) || change; // Beware of short-circuited logic.
        } while (change);

        states.retainAll(reachableStates);
        transitions.retainAll(reachableTransitions);
        actions.retainAll(reachableStates.stream().flatMap(s -> s.getActions().stream()).collect(toSet()));
    }

    /**
     * Checks whether the ELTS is deterministic. An ELTS is deterministic iff:
     *      ∀ v,v',v'' ∈ V, ∀z ∈ λ(v): [(v, v', z) ∈ ω and (v, v'', z) ∈ ω] → (v' = v'')
     *
     * @return Returns {@code true} if the ELTS is deterministic, otherwise {@code false} is returned.
     */
    public boolean isDeterministic() {
        for (final TransitionRelation<S, A> transition1 : transitions) {
            for (final TransitionRelation<S, A> transition2 : transitions) {
                if (transition1.from.equals(transition2.from) && transition1.trigger.equals(
                        transition2.trigger) && !Objects.equals(transition1.to, transition2.to)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Defines a custom string representation of the ELTS.
     *
     * @return Returns the string representation of the ELTS.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{\"initialState\":");
        sb.append(initialState);
        sb.append(",\"transitions\":[");

        boolean firstEntry = true;
        for (final TransitionRelation<S, A> transition : transitions) {
            if (!firstEntry) {
                sb.append(",");
            } else {
                firstEntry = false;
            }
            sb.append(transition);
        }
        sb.append("]}");
        return sb.toString();
    }
}
