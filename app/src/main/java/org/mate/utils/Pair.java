package org.mate.utils;

import java.util.Objects;

/**
 * Provides a generic pair class.
 *
 * @param <T> The type of the first entry.
 * @param <U> The type of the second entry.
 */
public final class Pair<T, U> {

    /**
     * The first entry of the pair.
     */
    public final T first;

    /**
     * The second entry of the pair.
     */
    public final U second;

    /**
     * Initialises a new pair with the given first and second entry.
     *
     * @param first The first entry of the pair.
     * @param second The second entry of the pair.
     */
    public Pair(final T first, final U second) {
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final Pair<?, ?> other = (Pair<?, ?>) o;
            return first.equals(other.first) && second.equals(other.second);
        }
    }

    @Override
    public int hashCode() {
        return 31 * first.hashCode() + second.hashCode();
    }
}
