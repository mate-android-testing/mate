package org.mate.exploration.genetic.selection;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.utils.Randomness;

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

        MATE.log_acc("Number of candidates: " + population.size());

        List<IChromosome<T>> selection = new ArrayList<>();
        List<IChromosome<T>> candidates = new LinkedList<>(population);
        int size = Math.min(Properties.DEFAULT_SELECTION_SIZE(), candidates.size());
        Comparator<IChromosome<T>> crowdedComparisonOperator = crowdedComparisonOperator(rankMap, crowdingDistanceMap);

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

    /**
     * Provides a comparison operators that considers both the rank and the crowding distance
     * of the chromosomes. The chromosome with the lower rank, or in case of equal rank with a higher
     * crowding distance, 'wins' the comparison.
     *
     * @param rankMap Maps each chromosome to its rank.
     * @param crowdingDistanceMap Maps each chromosome to its crowding distance.
     * @return Returns a value < 0 if o1 is better than o2, 0 if both are equal, or a value > 1 otherwise.
     */
    private Comparator<IChromosome<T>> crowdedComparisonOperator(final Map<IChromosome<T>, Integer> rankMap,
                                                                 final Map<IChromosome<T>, Double> crowdingDistanceMap) {

        return new Comparator<IChromosome<T>>() {
            @Override
            public int compare(IChromosome<T> o1, IChromosome<T> o2) {
                // a lower rank is better
                int rankCmp = Integer.compare(rankMap.get(o1), rankMap.get(o2));
                if (rankCmp != 0) {
                    return rankCmp;
                } else {
                    // a higher crowding distance is better
                    return Double.compare(crowdingDistanceMap.get(o2), crowdingDistanceMap.get(o1));
                }
            }
        };
    }
}
