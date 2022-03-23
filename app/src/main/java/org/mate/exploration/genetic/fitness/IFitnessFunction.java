package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;

/**
 * Interface for evaluating the fitness of a {@link IChromosome} used by {@link IGeneticAlgorithm}
 * and {@link GeneticAlgorithm}, respectively.
 *
 * @param <T> The type of the chromosome.
 */
public interface IFitnessFunction<T> {

    /**
     * Calculates the fitness for the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @return Returns the fitness of the chromosome.
     */
    double getFitness(IChromosome<T> chromosome);

    /**
     * Indicates if the fitness function is maximising or minimising.
     *
     * @return Returns {@code true} if the fitness function is maximising or {@code false} otherwise.
     */
    boolean isMaximizing();

    /**
     * Normalizes the fitness function to return a fitness value in the range [0,1].
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalized fitness value in range [0,1].
     */
    double getNormalizedFitness(IChromosome<T> chromosome);
}
