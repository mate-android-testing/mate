package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;

/**
 * Interface for evaluating the fitness of a {@link IChromosome} used by
 * {@link IGeneticAlgorithm} and {@link GeneticAlgorithm} respectively
 *
 * @param <T> Type wrapped by the chromosome implementation
 */
public interface IFitnessFunction<T> {
    /**
     * Calculate fitness for the chromosome
     *
     * @param chromosome chromosome to calculate fitness for
     * @return fitness of chromosome
     */
    double getFitness(IChromosome<T> chromosome);

    /**
     * Indicates if a fitness function is maximising or not.
     *
     * @return True if the function is maximising and false if not.
     */
    boolean isMaximizing();

    /**
     * Normalizes a fitness function in the range of [0,1] for a certain {@link IChromosome}.
     *
     * @param chromosome The chromosome to calculate the normalized fitness value in range [0,1].
     * @return The normalized value in range [0,1].
     */
    double getNormalizedFitness(IChromosome<T> chromosome);
}
