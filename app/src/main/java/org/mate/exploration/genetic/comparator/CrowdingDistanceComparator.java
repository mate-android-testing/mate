package org.mate.exploration.genetic.comparator;

import org.mate.exploration.genetic.chromosome.IChromosome;

import java.util.Comparator;
import java.util.Map;

/**
 * Provides a comparator that compares two {@link IChromosome}s based on their crowding distance.
 * The two {@link IChromosome}s are ordered in descending magnitude of the crowding distance.
 *
 * @param <T> The type of the chromosomes.
 */
public class CrowdingDistanceComparator<T> implements Comparator<IChromosome<T>> {

    /**
     * Maps each {@link IChromosome} to its crowding distance.
     */
    private final Map<IChromosome<T>, Double> crowdingDistanceMap;

    /**
     * Initialises the comparator with the crowding distance map.
     *
     * @param crowdingDistanceMap A mapping of chromosomes to its crowding distances.
     */
    public CrowdingDistanceComparator(Map<IChromosome<T>, Double> crowdingDistanceMap) {
        this.crowdingDistanceMap = crowdingDistanceMap;
    }

    /**
     * Compares two {@link IChromosome}s based on their crowding distance. The two chromosomes are
     * sorted in descending order of their crowding distances.
     *
     * @param o1 The first chromosome.
     * @param o2 The second chromosome.
     * @return Returns a value of {@code -1} if o2 has a lower crowding distance than o1 or a value
     *          of {@code 1} if o2 has a higher crowding distance than o1. If both chromosomes share
     *          the same crowding distance, a value of {@code 0} is returned.
     */
    @Override
    public int compare(IChromosome<T> o1, IChromosome<T> o2) {
        return Double.compare(crowdingDistanceMap.get(o2), crowdingDistanceMap.get(o1));
    }
}
