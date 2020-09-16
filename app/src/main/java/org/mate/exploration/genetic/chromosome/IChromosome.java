package org.mate.exploration.genetic.chromosome;

import android.support.annotation.NonNull;

import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;

/**
 * Interface for chromosomes used by {@link IGeneticAlgorithm} and {@link GeneticAlgorithm}
 * respectively
 * @param <T> Type wrapped by the chromosome implementation
 */
public interface IChromosome<T> {
    /**
     * Get value of the wrapped type
     * @return wrapped value
     */
    T getValue();

    @NonNull
    String toString();
}
