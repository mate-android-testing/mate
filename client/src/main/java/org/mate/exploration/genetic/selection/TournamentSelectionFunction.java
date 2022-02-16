package org.mate.exploration.genetic.selection;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.core.GAUtils;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a tournament selection function for a single objective that returns
 * {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
 *
 * @param <T> The type of the chromosomes.
 */
public class TournamentSelectionFunction<T> implements ISelectionFunction<T> {

    /**
     * The specified tournament size defined through the tournament size {@link org.mate.Properties}.
     */
    private final int tournamentSize;

    /**
     * Initialises the selection function with the given tournament size.
     *
     * @param tournamentSize The specified tournament size.
     */
    public TournamentSelectionFunction(int tournamentSize) {
        this.tournamentSize = tournamentSize;
    }

    /**
     * Performs a tournament selection for single objective search. The selection is repeated
     * until a selection of {@link Properties#DEFAULT_SELECTION_SIZE()} is formed.
     *
     * @param population The current population.
     * @param fitnessFunctions The list of fitness functions. Only the first one is used.
     * @return Returns {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
     */
    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions) {

        final IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(0);

        List<IChromosome<T>> selection = new ArrayList<>();
        List<IChromosome<T>> candidates = new LinkedList<>(population);
        int size = Math.min(Properties.DEFAULT_SELECTION_SIZE(), candidates.size());

        for (int i = 0; i < size; i++) {

            // pick tournament size many chromosomes randomly
            List<IChromosome<T>> randomElements = Randomness.randomElements(candidates, tournamentSize);

            // pick the best among those random elements
            IChromosome<T> best = GAUtils.getBest(randomElements, fitnessFunction);
            selection.add(best);

            // remove for next iteration
            candidates.remove(best);
        }

        return selection;
    }
}
