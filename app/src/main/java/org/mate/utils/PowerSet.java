package org.mate.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

/**
 * An efficient power set implementation based on an iterator.
 *
 * @param <E> The set type.
 */
public final class PowerSet<E> implements Iterator<Set<E>> {

    /**
     * The maximal size of the input set.
     */
    private final static int COUNTER_BIT_SIZE = 64;

    /**
     * The input set stored as a list.
     */
    private final List<E> elements;

    /**
     * The current position of the iterator.
     */
    private long counter;

    private boolean getCounterBit(final int index) {
        assert index < COUNTER_BIT_SIZE;
        return ((counter >>> index) & 1L) == 1L;
    }

    /**
     * Constructs a power set from the input set. The input set is not allowed to have more elements
     * than {@link #COUNTER_BIT_SIZE}.
     *
     * @param set The input set.
     */
    public PowerSet(final Set<E> set) {

        if (set.size() >= COUNTER_BIT_SIZE) {
            throw new IllegalArgumentException("Set too large.");
        }

        elements = new ArrayList<>(set);
        counter = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return counter < 1L << elements.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> next() {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        final Set<E> result = IntStream.range(0, elements.size())
                .filter(this::getCounterBit)
                .mapToObj(elements::get)
                .collect(toSet());
        ++counter;
        return result;
    }

    /**
     * Sets the iterator back to its initial position. This makes the iterator reusable.
     */
    public void reset() {
        counter = 0;
    }
}
