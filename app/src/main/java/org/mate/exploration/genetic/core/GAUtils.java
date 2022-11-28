package org.mate.exploration.genetic.core;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.comparator.CrowdedComparator;
import org.mate.exploration.genetic.comparator.CrowdingDistanceComparator;
import org.mate.exploration.genetic.comparator.DominationComparator;
import org.mate.exploration.genetic.comparator.FitnessAndLengthComparator;
import org.mate.exploration.genetic.comparator.FitnessComparator;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides utility functions in the context genetic algorithms, e.g. retrieving the best individual
 * of a population or sorting a population based on their fitness values.
 */
public class GAUtils {

    /**
     * Retrieves the best chromosome from the list of chromosomes according to the given fitness function.
     *
     * @param chromosomes A non-empty list of chromosomes.
     * @param fitnessFunction The given fitness function.
     * @param <T> The type of the chromosomes.
     * @return Returns the best chromosome from the given list of chromosomes.
     */
    public static <T> IChromosome<T> getBest(List<IChromosome<T>> chromosomes, IFitnessFunction<T> fitnessFunction) {

        if (chromosomes.isEmpty()) {
            throw new IllegalStateException("Can't retrieve best chromosome from empty list!");
        }

        // cache the fitness values
        Map<IChromosome<T>, Double> cache = new HashMap<>();

        final boolean isMaximizing = fitnessFunction.isMaximizing();
        IChromosome<T> best = null;

        for (IChromosome<T> chromosome : chromosomes) {

            if (best == null) {
                best = chromosome;
                cache.put(best, fitnessFunction.getNormalizedFitness(best));
            } else {

                double bestFitness = cache.get(best);
                double fitness = cache.containsKey(chromosome)
                        ? cache.get(chromosome) : fitnessFunction.getNormalizedFitness(chromosome);
                cache.put(chromosome, fitness);

                if (isMaximizing ? fitness > bestFitness : fitness < bestFitness) {
                    best = chromosome;
                }
            }
        }

        return best;
    }

    /**
     * Sorts the chromosomes based on its fitness values in ascending order, i.e. the chromosome with
     * the worst fitness value comes first, followed by the second worst chromosome and so on.
     * NOTE: This method sorts the chromosomes in place.
     *
     * @param chromosomes The given list of chromosomes to be sorted.
     * @param fitnessFunction The given fitness function.
     * @param <T> The type of the chromosomes.
     * @return Returns the chromosomes sorted based on its fitness values.
     */
    public static <T> List<IChromosome<T>> sortByFitness(
            List<IChromosome<T>> chromosomes, final IFitnessFunction<T> fitnessFunction) {
        Collections.sort(chromosomes, new FitnessComparator<>(fitnessFunction));
        return chromosomes;
    }

    /**
     * Sorts the chromosomes based on fitness (primary criteria) and length (secondary criteria).
     * The chromosomes are sorted in ascending order of fitness and descending order of length,
     * i.e. a chromosome with a worse fitness and a higher length comes first.
     * NOTE: This method sorts the chromosomes in place.
     *
     * @param chromosomes The given list of chromosomes to be sorted.
     * @param fitnessFunction The given fitness function.
     * @param <T> The type of the chromosomes. Only {@link TestCase}s and {@link TestSuite}s are supported.
     * @return Returns the chromosomes sorted based on its fitness values and its lengths.
     */
    public static <T> List<IChromosome<T>> sortByFitnessAndLength(
            List<IChromosome<T>> chromosomes, final IFitnessFunction<T> fitnessFunction) {
        Collections.sort(chromosomes, new FitnessAndLengthComparator<>(fitnessFunction));
        return chromosomes;
    }

    /**
     * Sorts the chromosomes based on the rank and the crowding distance. The chromosomes are sorted
     * in ascending order of the rank and in descending order of the crowding distance.
     * NOTE: This method sorts the chromosomes in place.
     *
     * @param chromosomes The given list of chromosomes to be sorted.
     * @param crowdingDistanceMap A mapping of chromosomes to its crowding distance.
     * @param <T> The type of the chromosomes. Only {@link TestCase}s and {@link TestSuite}s are supported.
     * @return Returns the chromosomes sorted based on its ranks and the crowding distances.
     */
    @SuppressWarnings("unused")
    public static <T> List<IChromosome<T>> sortByRankAndCrowdingDistance(
            List<IChromosome<T>> chromosomes,
            final Map<IChromosome<T>, Double> crowdingDistanceMap,
            final Map<IChromosome<T>, Integer> rankMap) {
        Collections.sort(chromosomes, new CrowdedComparator<>(crowdingDistanceMap, rankMap));
        return chromosomes;
    }

    /**
     * Sorts the chromosomes based on the crowding distance. The chromosomes are sorted in descending
     * order of the crowding distance.
     * NOTE: This method sorts the chromosomes in place.
     *
     * @param chromosomes The given list of chromosomes to be sorted.
     * @param crowdingDistanceMap A mapping of chromosomes to its crowding distance.
     * @param <T> The type of the chromosomes. Only {@link TestCase}s and {@link TestSuite}s are supported.
     * @return Returns the chromosomes sorted based on its crowding distance.
     */
    public static <T> List<IChromosome<T>> sortByCrowdingDistance(
            List<IChromosome<T>> chromosomes, final Map<IChromosome<T>, Double> crowdingDistanceMap) {
        Collections.sort(chromosomes, new CrowdingDistanceComparator<>(crowdingDistanceMap));
        return chromosomes;
    }

    /**
     * Performs the fast-non-dominated-sort algorithms as outlined on the bottom of the page 184.
     *
     * @param population The population P to be sorted based on the domination relation.
     * @return Returns the individual pareto fronts.
     */
    public static <T> Map<Integer, List<IChromosome<T>>> fastNonDominatedSort(
            final List<IChromosome<T>> population, final List<IFitnessFunction<T>> fitnessFunctions) {

        // associates the rank (pareto front) to the list of chromosomes belonging to it
        Map<Integer, List<IChromosome<T>>> paretoFronts = new HashMap<>();

        // a comparator for two chromosomes based on domination relation
        Comparator<IChromosome<T>> comparator = new DominationComparator<>(fitnessFunctions);

        // maintains for each chromosome the set of dominated chromosomes
        Map<IChromosome<T>, Set<IChromosome<T>>> dominationSets = new HashMap<>();

        // maintains for each chromosome the domination counter
        Map<IChromosome<T>, Integer> dominationCounters = new HashMap<>();

        for (IChromosome<T> p : population) {

            Set<IChromosome<T>> dominatedSolutions = new HashSet<>();
            int dominationCtr = 0;

            for (IChromosome<T> q : population) {

                if (p.equals(q)) {
                    // p can't dominate itself
                    continue;
                }

                int domination = comparator.compare(p, q);

                if (domination > 0) {
                    // p dominates q
                    dominatedSolutions.add(q);
                } else if (domination < 0) {
                    // q dominates p
                    dominationCtr++;
                }
            }

            dominationSets.put(p, dominatedSolutions);
            dominationCounters.put(p, dominationCtr);

            if (dominationCtr == 0) {

                // p belongs to the first front
                int rank = 1;

                if (paretoFronts.containsKey(rank)) {
                    paretoFronts.get(rank).add(p);
                } else {
                    List<IChromosome<T>> firstParetoFront = new LinkedList<>();
                    firstParetoFront.add(p);
                    paretoFronts.put(rank, firstParetoFront);
                }
            }
        }

        // derive the remaining pareto fronts
        int rank = 1;
        List<IChromosome<T>> currentParetoFront = paretoFronts.get(rank);

        while (!currentParetoFront.isEmpty()) {

            List<IChromosome<T>> nextParetoFront = new ArrayList<>();

            for (IChromosome<T> p : currentParetoFront) {
                for (IChromosome<T> q : dominationSets.get(p)) {

                    int dominationCtr = dominationCounters.get(q) - 1;
                    dominationCounters.put(q, dominationCtr);

                    if (dominationCtr == 0) {
                        // q belongs to the next front (actually all duplicates as well)
                        for (IChromosome<T> chromosome : population) {
                            if (chromosome.equals(q)) {
                                nextParetoFront.add(q);
                            }
                        }
                    }
                }
            }

            rank = rank + 1;
            if (!nextParetoFront.isEmpty()) {
                paretoFronts.put(rank, nextParetoFront);
            }

            currentParetoFront = nextParetoFront;
        }

        return paretoFronts;
    }

    /**
     * Performs the crowding-distance-assignment procedure as described on the bottom of page 185.
     *
     * @param population The population for which the crowding distance should be assigned.
     * @param fitnessFunctions The list of objective (fitness) functions.
     * @return Returns a mapping of chromosomes to its crowding distance values.
     */
    public static <T> Map<IChromosome<T>, Double> crowdingDistanceAssignment(List<IChromosome<T>> population,
                                                                  List<IFitnessFunction<T>> fitnessFunctions) {

        List<IChromosome<T>> solutions = new LinkedList<>(population);
        int length = population.size();

        Map<IChromosome<T>, Double> crowdingDistanceAssignments = new HashMap<>();

        // init crowding distances
        for (IChromosome<T> solution : solutions) {
            crowdingDistanceAssignments.put(solution, 0.0);
        }

        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {

            // sort in ascending order of magnitude
            GAUtils.sortByFitness(solutions, fitnessFunction);

            double worstFitnessValue = fitnessFunction.getNormalizedFitness(solutions.get(0));
            double bestFitnessValue = fitnessFunction.getNormalizedFitness(solutions.get(length - 1));

            // set crowding distance of boundary solutions to infinity (these solutions will be always included)
            int start = 0;
            int end = length - 1;

            for (int i = 0; i < length; i++) {

                IChromosome<T> solution = solutions.get(i);
                double fitness = fitnessFunction.getNormalizedFitness(solution);

                if (fitness == worstFitnessValue) {
                    crowdingDistanceAssignments.put(solution, Double.POSITIVE_INFINITY);
                    start = i;
                } else if (fitness == bestFitnessValue) {
                    crowdingDistanceAssignments.put(solution, Double.POSITIVE_INFINITY);
                    if (i < end) {
                        end = i;
                    }
                }
            }

            // assign crowding distance to every other solution
            for (int i = start + 1; i < end; i++) {

                IChromosome<T> solution = solutions.get(i);

                double predecessorFitness = fitnessFunction.getNormalizedFitness(solutions.get(i - 1));
                double successorFitness = fitnessFunction.getNormalizedFitness(solutions.get(i + 1));

                if (!fitnessFunction.isMaximizing()) {
                    // flip fitness values
                    predecessorFitness = 1 - predecessorFitness;
                    successorFitness = 1 - successorFitness;
                }

                /*
                 * The actual formula is as follows:
                 *
                 * I[i](distance) = I[i](distance) + (I[i+1].m - I[i-1].m) / (fmax - fmin)
                 *
                 * where m refers to the mth objective (fitness) function and fmax and fmin to
                 * the maximal and minimal fitness value of the mth objective function.
                 *
                 * Since we use here the normalised fitness values bounded in [0,1], the last
                 * term (fmax - fmin) gets resolved to 1 - 0 = 1, hence a redundant division by 1.
                 */
                double crowdingDistance = crowdingDistanceAssignments.get(solution)
                        + successorFitness - predecessorFitness;
                crowdingDistanceAssignments.put(solution, crowdingDistance);
            }
        }

        return crowdingDistanceAssignments;
    }

    /**
     * Converts the pareto fronts into a rank map.
     *
     * @param paretoFronts The given pareto fronts.
     * @return Returns a mapping of chromosomes to its rank, i.e. the index of the pareto front.
     */
    public static <T> Map<IChromosome<T>, Integer> getRankMap(Map<Integer, List<IChromosome<T>>> paretoFronts) {

        Map<IChromosome<T>, Integer> rankMap = new HashMap<>();

        for (Map.Entry<Integer, List<IChromosome<T>>> paretoFront : paretoFronts.entrySet()) {
            int rank = paretoFront.getKey();
            for (IChromosome<T> solution : paretoFront.getValue()) {
                rankMap.put(solution, rank);
            }
        }

        return rankMap;
    }
}
