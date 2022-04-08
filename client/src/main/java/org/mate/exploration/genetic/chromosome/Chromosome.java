package org.mate.exploration.genetic.chromosome;

import androidx.annotation.NonNull;

/**
 * Provides an implementation of a chromosome.
 *
 * @param <T> The type wrapped by the chromosome.
 */
public class Chromosome<T> implements IChromosome<T> {

    /**
     * The wrapped testing instance.
     */
    private final T value;

    /**
     * Initialises a new chromosome which encapsulates the given value.
     *
     * @param value The value wrapped by the chromosome.
     */
    public Chromosome(T value) {
        this.value = value;
    }

    /**
     * Returns the value wrapped by the chromosome.
     *
     * @return Returns the value wrapped by the chromosome.
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * Provides a string representation of the chromosome.
     *
     * @return Returns the string representation of the chromosome.
     */
    @NonNull
    @Override
    public String toString() {
        return value.toString();
    }
}
