package org.mate.exploration.genetic;

/**
 * Interface for generating {@link IChromosome}s used by {@link IGeneticAlgorithm} and
 * {@link GeneticAlgorithm} respectively
 * @param <T> Type wrapped by the chromosome implementation
 */
public interface IChromosomeFactory<T> {
    /**
     * Generate a chromosome
     * @return generated chromosome
     */
    IChromosome<T> createChromosome();
}
