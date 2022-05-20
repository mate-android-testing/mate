package org.mate.exploration.genetic.selection;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.comparator.CrowdedComparator;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides crowded tournament selection operator as described in the NSGA-II paper.
 *
 * @param <T> The type of the chromosomes.
 */
public class CrowdedTournamentSelectionFunction<T> implements ISelectionFunction<T> {

    /**
     * Do not call this method, consider instead {@link #select(List, Map, Map)}.
     *
     * @param population The current population used as candidates for the selection.
     * @param fitnessFunctions The list of fitness functions.
     * @return Throws an {@link UnsupportedOperationException}.
     */
    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    /**
     * Performs a binary tournament selection that considers both rank and the crowding distance
     * as suggested in the NSGA-II algorithm.
     *
     * @param population The current population.
     * @param rankMap Maps each chromosome to its rank.
     * @param crowdingDistanceMap Maps each chromosome to its crowding distance.
     * @return Returns {@link Properties#DEFAULT_SELECTION_SIZE()} chromosomes.
     */
    public List<IChromosome<T>> select(List<IChromosome<T>> population,
                                       Map<IChromosome<T>, Integer> rankMap,
                                       Map<IChromosome<T>, Double> crowdingDistanceMap) {

        List<IChromosome<T>> selection = new ArrayList<>();
        List<IChromosome<T>> candidates = new LinkedList<>(population);
        int size = Math.min(Properties.DEFAULT_SELECTION_SIZE(), candidates.size());
        Comparator<IChromosome<T>> crowdedComparisonOperator = new CrowdedComparator<>(crowdingDistanceMap, rankMap);

        for (int i = 0; i < size; i++) {

            // pick two chromosomes randomly
            List<IChromosome<T>> randomElements = Randomness.randomElements(candidates, 2);

            // pick the best among those two chromosomes if one is better, otherwise take the second
            int cmp = crowdedComparisonOperator.compare(randomElements.get(0), randomElements.get(1));
            IChromosome<T> best = cmp < 0 ? randomElements.get(0) : randomElements.get(1);
            selection.add(best);

            // remove for next iteration
            candidates.remove(best);
        }

        return selection;
    }
}
