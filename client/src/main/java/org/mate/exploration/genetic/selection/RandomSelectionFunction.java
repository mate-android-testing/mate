package org.mate.exploration.genetic.selection;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a random selection. Returns {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
 *
 * @param <T> The type of the chromosomes.
 */
public class RandomSelectionFunction<T> implements ISelectionFunction<T> {

    /**
     * Performs a random selection that returns up to {@link Properties#DEFAULT_SELECTION_SIZE()}
     * chromosomes.
     *
     * @param population The current population.
     * @param fitnessFunctions The list of fitness functions. Unused here.
     * @return Returns {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
     */
    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions) {
        List<IChromosome<T>> candidates = new ArrayList<>(population);
        Randomness.shuffleList(candidates);
        return candidates.subList(0, Math.min(Properties.DEFAULT_SELECTION_SIZE(), candidates.size()));
    }
}
