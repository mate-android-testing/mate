package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;

/**
 * Extends a {@link IFitnessFunction} with the ability to retrieve the fitness for a range of actions.
 *
 * @param <T> The type wrapped by the chromosomes.
 */
public interface IActionFitnessFunction<T> extends IFitnessFunction<T> {

    /**
     * Retrieves the fitness value for the specified range of actions of the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @param actions The range of actions starting at 0.
     * @return Returns the fitness value for the given chromosome.
     */
    double getFitness(IChromosome<T> chromosome, int actions);

    /**
     * Retrieves the normalised fitness value for the specified range of actions of the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @param actions The range of actions starting at 0.
     * @return Returns the normalised fitness value for the given chromosome.
     */
    double getNormalizedFitness(IChromosome<T> chromosome, int actions);
}
