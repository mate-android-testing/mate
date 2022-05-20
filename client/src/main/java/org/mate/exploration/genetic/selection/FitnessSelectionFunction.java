package org.mate.exploration.genetic.selection;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.core.GAUtils;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides a selection based on the order of the fitness values for single objective search.
 * Returns {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
 *
 * @param <T> The type of the chromosomes.
 */
public class FitnessSelectionFunction<T> implements ISelectionFunction<T> {

    /**
     * Performs a selection based on the order of the fitness values.
     *
     * @param population The current population.
     * @param fitnessFunctions The list of fitness functions. Only the first one is used here.
     * @return Returns {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
     */
    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, final List<IFitnessFunction<T>> fitnessFunctions) {

        final IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(0);
        List<IChromosome<T>> candidates = new ArrayList<>(population);

        /*
         * We shuffle the list that chromosomes with an identical fitness value get a 'fair'
         * chance to be selected.
         */
        Randomness.shuffleList(candidates);

        // sort in descending order, i.e. the best chromosomes come first
        List<IChromosome<T>> sorted = GAUtils.sortByFitness(candidates, fitnessFunction);
        Collections.reverse(sorted);

        return sorted.subList(0, Math.min(Properties.DEFAULT_SELECTION_SIZE(), sorted.size()));
    }
}
