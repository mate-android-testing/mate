package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;

import java.util.List;

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
    float getFitness(IChromosome<T> chromosome, int actions);

    /**
     * Retrieves the normalised fitness value for the specified range of actions of the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @param actions The range of actions starting at 0.
     * @return Returns the normalised fitness value for the given chromosome.
     */
    float getNormalizedFitness(IChromosome<T> chromosome, int actions);

    /**
     * Retrieves the normalised fitness vector of the given chromosome, i.e., a vector consisting
     * of the individual action fitness values in action order.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @return Returns the normalised fitness vector for the given chromosome.
     */
    List<Float> getNormalizedFitnessVector(IChromosome<T> chromosome);

    /**
     * Retrieves the index of the associated fitness function.
     *
     * @return Returns the index of the associated fitness function.
     */
    int getIndex();
}
