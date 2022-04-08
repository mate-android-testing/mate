package org.mate.exploration.genetic.chromosome;

import androidx.annotation.NonNull;

/**
 * Provides the interface for a chromosome. A chromosome is simply a wrapper around the actual
 * testing instance, e.g. a test case.
 *
 * @param <T> The type wrapped by a chromosome.
 */
public interface IChromosome<T> {

    /**
     * Returns the wrapped type.
     *
     * @return Returns the wrapped testing instance, e.g. a test case object.
     */
    T getValue();

    /**
     * Provides a string representation of the chromosome.
     *
     * @return Returns the string representation of the chromosome.
     */
    @NonNull
    String toString();
}
