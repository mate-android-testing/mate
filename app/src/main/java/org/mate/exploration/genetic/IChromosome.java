package org.mate.exploration.genetic;

/**
 * Interface for chromosomes used by {@link IGeneticAlgorithm} and {@link GeneticAlgorithm}
 * respectively
 * @param <T> Type wrapped by the chromosome implementation
 */
interface IChromosome<T> {
    /**
     * Get value of the wrapped type
     * @return wrapped value
     */
    T getValue();
}
