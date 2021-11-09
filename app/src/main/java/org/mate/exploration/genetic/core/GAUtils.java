package org.mate.exploration.genetic.core;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.comparator.DominationComparator;
import org.mate.exploration.genetic.comparator.FitnessComparator;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

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
    public static <T> List<IChromosome<T>> sortByFitness(List<IChromosome<T>> chromosomes,
                                                         final IFitnessFunction<T> fitnessFunction) {
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
    public static <T> List<IChromosome<T>> sortByFitnessAndLength(List<IChromosome<T>> chromosomes,
                                                         final IFitnessFunction<T> fitnessFunction) {

        final boolean isMaximizing = fitnessFunction.isMaximizing();
        Collections.sort(chromosomes, new Comparator<IChromosome<T>>() {
            @Override
            public int compare(IChromosome<T> o1, IChromosome<T> o2) {

                double fitnessChromosome1 = fitnessFunction.getNormalizedFitness(o1);
                double fitnessChromosome2 = fitnessFunction.getNormalizedFitness(o2);

                if (fitnessChromosome1 == fitnessChromosome2) {

                    // compare on length, sorts in descending order
                    if (o1.getValue() instanceof TestCase) {
                        return ((TestCase) o2.getValue()).getEventSequence().size()
                                - ((TestCase) o1.getValue()).getEventSequence().size();
                    } else if (o1.getValue() instanceof TestSuite) {
                        return ((TestSuite) o2.getValue()).getTestCases().size()
                                - ((TestSuite) o1.getValue()).getTestCases().size();
                    } else {
                        throw new IllegalStateException("Chromosome type " + o1.getValue().getClass()
                                + "not yet supported!");
                    }

                } else {

                    // compare on fitness, sorts in ascending order
                    if (isMaximizing) {
                        return Double.compare(fitnessChromosome1, fitnessChromosome2);
                    } else {
                        return Double.compare(fitnessChromosome2, fitnessChromosome1);
                    }
                }
            }
        });

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

        MATE.log_acc("Fast-Non-Dominated-Sort...");

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
        int i = 1;
        List<IChromosome<T>> nextParetoFront = paretoFronts.get(i);
        MATE.log_acc("First pareto front: " + nextParetoFront);

        while (!nextParetoFront.isEmpty()) {

            MATE.log_acc("Pareto front " + i + ": " + nextParetoFront.size());

            List<IChromosome<T>> nextFront = new LinkedList<>();

            for (IChromosome<T> p : nextParetoFront) {
                for (IChromosome<T> q : dominationSets.get(p)) {
                    int dominationCtr = dominationCounters.get(q) - 1;
                    dominationCounters.put(q, dominationCtr);

                    MATE.log_acc("Chromosome " + p + " dominates " + q);
                    MATE.log_acc("New domination counter: " + dominationCtr);

                    if (dominationCtr == 0) {
                        // q belongs to the next front
                        nextFront.add(q);
                    }
                }
            }

            i = i + 1;
            nextParetoFront = nextFront;

            if (!nextParetoFront.isEmpty()) {
                MATE.log_acc("Pareto front: " + nextParetoFront);
                paretoFronts.put(i, nextParetoFront);
            }
        }

        MATE.log_acc("Number of pareto fronts: " + paretoFronts.size());
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

        MATE.log_acc("Crowding distance assignment...");

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

        MATE.log_acc("Getting rank map...");

        Map<IChromosome<T>, Integer> rankMap = new HashMap<>();

        for (Map.Entry<Integer, List<IChromosome<T>>> paretoFront : paretoFronts.entrySet()) {
            int rank = paretoFront.getKey();
            MATE.log_acc("Transforming front " + rank + " with " + paretoFront.getValue().size() + " members!");
            for (IChromosome<T> solution : paretoFront.getValue()) {
                rankMap.put(solution, rank);
            }
        }

        MATE.log_acc("Rank map size: " + rankMap.size());
        return rankMap;
    }
}
