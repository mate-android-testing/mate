package org.mate.exploration.genetic.crossover;

import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.chromosome.IChromosome;

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
