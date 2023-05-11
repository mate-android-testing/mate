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
 * A Trace is a sequence of transitions taken by a test case.
 * Essentially a fancy warper for a {@code List<Transition>}.
 */
public final class Trace implements Iterable<Transition> {

    private final List<Transition> transitions;

    public Trace() {
        this.transitions = new ArrayList<>();
    }

    public Trace(final List<Transition> transitions) {
        this.transitions = new ArrayList<>(transitions);
    }

    public Trace enqueue(final Transition transition) {
        transitions.add(requireNonNull(transition));
        return this;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    /**
     * For each state visited in the trace, count how often each action was executed in that state
     * and add the counts to the frequency map.
     *
     * @param freq The map that holds the total acummulated action counts.
     * @param traces The traces which actions should be counted.
     */
    public static void countFrequencies(final Map<State, Map<Action, Integer>> freq,
                                        final Collection<Trace> traces) {
        requireNonNull(freq);

        for (Trace trace : traces) {
            for (Transition transition : trace) {
                final State source = transition.getSource();
                final Action action = transition.getAction();
                final Map<Action, Integer> actions = freq.computeIfAbsent(source, ignored -> new HashMap<>());
                actions.merge(action, 1, Integer::sum);
            }
        }
    }

    public Stream<Transition> stream() {
        return transitions.stream();
    }

    public int size() {
        return transitions.size();
    }

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

    @NonNull
    @Override
    public Iterator<Transition> iterator() {
        return transitions.iterator();
    }
}

