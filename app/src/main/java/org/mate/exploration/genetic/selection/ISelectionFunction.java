package org.mate.exploration.genetic.selection;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;

import java.util.List;

/**
 * Interface for selecting {@link IChromosome}s that should later be used for
 * {@link ICrossOverFunction} and {@link IMutationFunction}. The respective selection function
 * should return up to {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
 *
 * @param <T> The type of the chromosomes.
 */
public interface ISelectionFunction<T> {

    /**
     * Selects up to {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes that are later used
     * for crossover {@link ICrossOverFunction} and mutation {@link IMutationFunction}.
     *
     * @param population The current population used as candidates for the selection.
     * @param fitnessFunctions The list of fitness functions. In case of single objective search
     *                         only the first fitness function is used.
     * @return Returns up to {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
     */
    List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions);
}
