package org.mate.model.fsm.sosm;

import android.support.annotation.NonNull;

import org.mate.interaction.action.Action;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Describes a sequence of transitions taken by a test case.
 */
public final class Trace implements Iterable<Transition> {

    /**
     * The list of transitions traversed by the test case.
     */
    private final List<Transition> transitions;

    /**
     * Creates a new empty trace.
     */
    public Trace() {
        this.transitions = new ArrayList<>();
    }

    /**
     * Creates a new trace with the given transitions.
     *
     * @param transitions The list of of transitions.
     */
    public Trace(final List<Transition> transitions) {
        this.transitions = new ArrayList<>(transitions);
    }

    /**
     * Adds a new transition to the existing trace.
     *
     * @param transition The new transition.
     * @return Returns the resulting trace.
     */
    public Trace enqueue(final Transition transition) {
        transitions.add(requireNonNull(transition));
        return this;
    }

    /**
     * Returns the list of transitions taken by the test case.
     *
     * @return Returns the list of transitions taken by the test case.
     */
    public List<Transition> getTransitions() {
        return transitions;
    }

    /**
     * Counts how often an action has been executed in a particular state.
     *
     * @param frequencies The map that holds the total accumulated action counts per state.
     * @param traces The traces which actions should be counted.
     */
    public static void countFrequencies(final Map<State, Map<Action, Integer>> frequencies,
                                        final Collection<Trace> traces) {
        requireNonNull(frequencies);

        for (Trace trace : traces) {
            for (Transition transition : trace) {
                final State source = transition.getSource();
                final Action action = transition.getAction();
                final Map<Action, Integer> actions
                        = frequencies.computeIfAbsent(source, ignored -> new HashMap<>());
                actions.merge(action, 1, Integer::sum);
            }
        }
    }

    /**
     * Returns the trace as a stream of transitions.
     *
     * @return Returns the trace as a stream of transitions.
     */
    public Stream<Transition> stream() {
        return transitions.stream();
    }

    /**
     * Returns the the length of the trace, i.e. the number of transitions.
     *
     * @return Returns the trace' size.
     */
    public int size() {
        return transitions.size();
    }

    /**
     * Checks whether the trace is empty, i.e. no transitions have been added yet.
     *
     * @return Returns {@code true} if empty, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String toString() {
        return String.format("Trace{transitions=%s}", transitions);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final Trace trace = (Trace) o;
            return transitions.equals(trace.transitions);
        }
    }

    @Override
    public int hashCode() {
        return transitions.hashCode();
    }

    /**
     * Returns an iterator over the transitions.
     *
     * @return Returns an iterator over the transitions.
     */
    @NonNull
    @Override
    public Iterator<Transition> iterator() {
        return transitions.iterator();
    }
}

