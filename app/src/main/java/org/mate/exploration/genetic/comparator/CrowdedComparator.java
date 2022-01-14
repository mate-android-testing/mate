package org.mate.exploration.genetic.comparator;

import org.mate.exploration.genetic.chromosome.IChromosome;

import java.util.Comparator;
import java.util.Map;

/**
 * Provides a comparator that compares two {@link IChromosome}s based on the rank and the crowding
 * distance. Sorts the two chromosomes in ascending order of its ranks and in descending order of
 * its crowding distances.
 *
 * @param <T> The type of the chromosomes.
 */
public class CrowdedComparator<T> implements Comparator<IChromosome<T>> {

    /**
     * Maps each {@link IChromosome} to its crowding distance.
     */
    private final Map<IChromosome<T>, Double> crowdingDistanceMap;

    /**
     * Maps each {@link IChromosome} to its rank.
     */
    private final Map<IChromosome<T>, Integer> rankMap;

    /**
     * Initialises the crowded comparator with the rank and crowding distance map.
     *
     * @param crowdingDistanceMap A mapping of chromosomes to its crowding distances.
     * @param rankMap A mapping of chromosomes to its ranks.
     */
    public CrowdedComparator(Map<IChromosome<T>, Double> crowdingDistanceMap,
                             Map<IChromosome<T>, Integer> rankMap) {
        this.crowdingDistanceMap = crowdingDistanceMap;
        this.rankMap = rankMap;
    }

    /**
     * Compares two {@link IChromosome}s based on their rank and the crowding distance. The two
     * chromosomes are sorted in ascending order of the rank and in descending order of the crowding
     * distance, i.e. a chromosome with a lower rank and a higher crowding distance comes first.
     *
     * @param o1 The first chromosome.
     * @param o2 The second chromosome.
     * @return Returns a value of {@code -1} if o1 has a lower rank than o2 or a value of {@code 1}
     *          if o2 has a higher rank than o1. If both chromosomes have the same rank, then the
     *          chromosome with the higher crowding distance is ordered first. If both chromosomes
     *          have the same rank and crowding distance, a value of {@code 0} is returned.
     */
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
}
