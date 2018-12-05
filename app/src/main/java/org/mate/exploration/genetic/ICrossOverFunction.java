package org.mate.exploration.genetic;

import java.util.List;

/**
 * Interface for performing a crossover on multiple {@link IChromosome} used by
 * {@link IGeneticAlgorithm} and {@link GeneticAlgorithm} respectively
 * @param <T> Type wrapped by the chromosome implementation
 */
public interface ICrossOverFunction<T> {
    /**
     * Cross the parents
     * @param parents parents used for the crossing
     * @return offspring
     */
    IChromosome<T> cross(List<IChromosome<T>> parents);
}
