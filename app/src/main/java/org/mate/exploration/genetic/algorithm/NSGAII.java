package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GAUtils;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.CrowdedTournamentSelectionFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mate.exploration.genetic.core.GAUtils.getParetoFront;
import static org.mate.exploration.genetic.core.GAUtils.updateCrowdingDistance;
import static org.mate.utils.MathUtils.isEpsEq;

/**
 * Provides an implementation of the NSGA-II algorithm as proposed in the paper
 * "A fast and elitist multiobjective genetic algorithm: NSGA-II", see for more details
 * https://ieeexplore.ieee.org/document/996017.
 *
 * @param <T> The type of the chromosomes.
 */
public class NSGAII<T> extends GeneticAlgorithm<T> {

    /**
     * NSGA-II uses a customized selection function that considers both the rank and the crowding
     * distance for the selection.
     */
    private final CrowdedTournamentSelectionFunction<T> selectionFunction;

    /**
     * Initialises the NSGA-II algorithm with the necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param selectionFunction The used selection function.
     * @param crossOverFunction The used crossover function.
     * @param mutationFunction The used mutation function.
     * @param fitnessFunctions The list of fitness functions.
     * @param terminationCondition The used termination condition.
     * @param populationSize The population size N.
     * @param bigPopulationSize The big population size 2N.
     * @param pCrossover The probability rate for crossover.
     * @param pMutate The probability rate for mutation.
     */
    public NSGAII(IChromosomeFactory<T> chromosomeFactory,
                  ISelectionFunction<T> selectionFunction,
                  ICrossOverFunction<T> crossOverFunction,
                  IMutationFunction<T> mutationFunction,
                  List<IFitnessFunction<T>> fitnessFunctions,
                  ITerminationCondition terminationCondition,
                  int populationSize,
                  int bigPopulationSize,
                  double pCrossover,
                  double pMutate) {
        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction,
                fitnessFunctions, terminationCondition, populationSize,
                bigPopulationSize, pCrossover, pMutate);
        this.selectionFunction = (CrowdedTournamentSelectionFunction<T>) selectionFunction;
    }

    /**
     * Provides a comparator that compares two chromosomes based on the domination relation.
     *
     * @param fitnessFunctions The list of fitness functions.
     * @return Returns a comparator for the domination relation.
     */
    private Comparator<IChromosome<T>> dominationComparator(final List<IFitnessFunction<T>> fitnessFunctions) {

        return new Comparator<IChromosome<T>>() {

            /**
             *  Performs a comparison of two chromosomes based on the domination relation.
             *
             * @param o1 The first chromosome.
             * @param o2 The second chromosome.
             * @return Returns a value of {@code -1} if o2 dominates o1 or a value of {@code 1}
             *          if o1 dominates o2. If no chromosome dominates each other, a value of
             *          {@code 0} is returned.
             */
            @Override
            public int compare(IChromosome<T> o1, IChromosome<T> o2) {

                /*
                * Whether o1 is in at least one target better/worse than o2.
                 */
                boolean isBetterInOneTarget = false;
                boolean isWorseInOneTarget = false;

                for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {

                    boolean isMaximising = fitnessFunction.isMaximizing();

                    double fitness1 = fitnessFunction.getNormalizedFitness(o1);
                    double fitness2 = fitnessFunction.getNormalizedFitness(o2);
                    int cmp = Double.compare(fitness1, fitness2);

                    if (cmp == 0) {
                        // compare on next target
                        continue;
                    } else {

                        if (!isMaximising) {
                            // flip comparison for minimising fitness function
                            cmp = cmp * -1;
                        }

                        if (cmp < 0) {
                            // o2 dominates o1
                            isWorseInOneTarget = true;
                        } else {
                            // o1 dominates o2
                            isBetterInOneTarget = true;
                        }

                        if (isBetterInOneTarget && isWorseInOneTarget) {
                            // no domination, hence same pareto front
                            return 0;
                        }
                    }
                }

                if (!isWorseInOneTarget && !isBetterInOneTarget) {
                    // equal in every target, hence no domination
                    return 0;
                } else if (isBetterInOneTarget && !isWorseInOneTarget) {
                    // at least better in one target, hence o1 dominates o2
                    return 1;
                } else if (isWorseInOneTarget && !isBetterInOneTarget) {
                    // at least worse in one target, hence o2 dominates o1
                    return -1;
                } else {
                    throw new IllegalStateException("Should never happen!");
                }
            }
        };
    }

    /**
     * Performs the fast-non-dominated-sort algorithms as outlined on the bottom of the page 184.
     *
     * @param population The population P to be sorted based on the domination relation.
     * @return Returns the individual pareto fronts.
     */
    private Map<Integer, List<IChromosome<T>>> fastNonDominatedSort(List<IChromosome<T>> population) {

        // associates the rank (pareto front) to the list of chromosomes belonging to it
        Map<Integer, List<IChromosome<T>>> paretoFronts = new HashMap<>();

        // a comparator for two chromosomes based on domination relation
        Comparator<IChromosome<T>> comparator = dominationComparator(fitnessFunctions);

        // maintains for each chromosome the set of dominated chromosomes
        Map<IChromosome<T>, Set<IChromosome<T>>> dominationSets = new HashMap<>();

        // maintains for each chromosome the domination counter
        Map<IChromosome<T>, Integer> dominationCounters = new HashMap<>();

        for (IChromosome<T> p : population) {

            Set<IChromosome<T>> dominatedSolutions = new HashSet<>();
            int dominationCtr = 0;

            for (IChromosome<T> q : population) {

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
                List<IChromosome<T>> firstParetoFront = new LinkedList<>();
                firstParetoFront.add(p);
                paretoFronts.put(rank, firstParetoFront);
            }
        }

        int i = 1;
        List<IChromosome<T>> nextParetoFront = paretoFronts.get(i);

        while (!nextParetoFront.isEmpty()) {

            List<IChromosome<T>> nextFront = new LinkedList<>();

            for (IChromosome<T> p : nextParetoFront) {
                for (IChromosome<T> q : dominationSets.get(p)) {
                    int dominationCtr = dominationCounters.get(q) - 1;
                    dominationCounters.put(q, dominationCtr);
                    if (dominationCtr == 0) {
                        // q belongs to the next front
                        int rank = i + 1;
                        nextFront.add(q);
                    }
                }
            }
            i = i + 1;
            nextParetoFront = nextFront;
            paretoFronts.put(i, nextParetoFront);
        }

        return paretoFronts;
    }

    /**
     * NSGA-II follows the same procedure as in a {@link StandardGeneticAlgorithm} except the
     * additional computation of the pareto fronts and the crowding distance measurement.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));
        List<IChromosome<T>> newGeneration = new ArrayList<>(population);

        /*
        * We only need to compute the fronts F (= F1, F2,...) and the crowding distances once
        * for the selection function.
         */
        Map<Integer, List<IChromosome<T>>> paretoFronts = fastNonDominatedSort(population);
        Map<IChromosome<T>, Integer> rankMap = getRankMap(paretoFronts);
        Map<IChromosome<T>, Double> crowdingDistanceMap = crowdingDistanceAssignment(population, fitnessFunctions);

        while (newGeneration.size() < bigPopulationSize) {

            // performs a binary tournament selection that considers both rank and crowding distance
            List<IChromosome<T>> parents = selectionFunction.select(population, rankMap, crowdingDistanceMap);

            IChromosome<T> parent;

            if (Randomness.getRnd().nextDouble() < pCrossover) {
                parent = crossOverFunction.cross(parents);
            } else {
                parent = parents.get(0);
            }

            IChromosome<T> offspring;

            if (Randomness.getRnd().nextDouble() < pMutate) {
                offspring = mutationFunction.mutate(parent);
            } else {
                offspring = parent;
            }

            newGeneration.add(offspring);
        }

        // TODO: beautify later when more time
        population.clear();
        population.addAll(newGeneration);
        List<IChromosome<T>> survivors = getGenerationSurvivors();
        population.clear();
        population.addAll(survivors);
        logCurrentFitness();
        currentGenerationNumber++;
    }

    /**
     * Retrieves the survivors for the next generation. In NSGA-II the new population is filled
     * with the solutions from the first pareto fronts. The (last) front that could not be fully
     * accommodated in the new population due to the size constraint {@link #populationSize}
     * requires a special treatment. That particular front will be sorted based on the crowding
     * distance assignment procedure and only those solutions with the highest crowding distance
     * will be added to the new population until the {@link #populationSize} is reached.
     *
     * @return Returns the new population for the next generation.
     */
    @Override
    public List<IChromosome<T>> getGenerationSurvivors() {

        /*
        * The current population consists of 2*N solutions where N refers to the population size, but
        * only N solutions can be part of the next generation. NSGA-II sorts the population based
        * on the domination relation and fills the new population with solutions from the first new
        * fronts until the population size of N is reached. The last front that couldn't be fully
        * accommodated in the new population requires a special treatment. Only the least crowded
        * solutions in the last front will be added to the new population until the population size
        * of N is reached.
         */
        List<IChromosome<T>> survivors = new LinkedList<>();
        Map<Integer, List<IChromosome<T>>> paretoFronts = fastNonDominatedSort(population);
        int i = 1;

        MATE.log_acc("Get generation survivors...");
        MATE.log_acc("Population size: " + population.size());
        MATE.log_acc("Pareto fronts: " + paretoFronts.size());

        for (Map.Entry<Integer, List<IChromosome<T>>> paretoFront : paretoFronts.entrySet()) {
            MATE.log_acc("Pareto front: " + paretoFront.getKey());
            MATE.log_acc("Pareto front members: " + paretoFront.getValue().size());
        }

        // add solutions until a front can't be fully accommodated
        while (survivors.size() + paretoFronts.get(i).size() < populationSize) {
            survivors.addAll(paretoFronts.get(i));
            i = i + 1;
        }

        // sort last front in descending order of crowding distance
        List<IChromosome<T>> lastFront = paretoFronts.get(i);
        final Map<IChromosome<T>, Double> crowdingDistances
                = crowdingDistanceAssignment(lastFront, fitnessFunctions);

        Collections.sort(lastFront, new Comparator<IChromosome<T>>() {
            @Override
            public int compare(IChromosome<T> o1, IChromosome<T> o2) {
                return Double.compare(crowdingDistances.get(o2), crowdingDistances.get(o1));
            }
        });

        // fill up the remaining slots with the least crowded chromosomes of the last front
        survivors.addAll(lastFront.subList(0, populationSize - lastFront.size()));
        return survivors;
    }

    /**
     * Converts the pareto fronts into a rank map.
     *
     * @param paretoFronts The given pareto fronts.
     * @return Returns a mapping of chromosomes to its rank, i.e. the index of the pareto front.
     */
    private Map<IChromosome<T>, Integer> getRankMap(Map<Integer, List<IChromosome<T>>> paretoFronts) {

        MATE.log_acc("Pareto fronts: " + paretoFronts.size());

        Map<IChromosome<T>, Integer> rankMap = new HashMap<>();

        for (Map.Entry<Integer, List<IChromosome<T>>> paretoFront : paretoFronts.entrySet()) {
            int rank = paretoFront.getKey();
            for (IChromosome<T> solution : paretoFront.getValue()) {
                rankMap.put(solution, rank);
            }
        }

        MATE.log_acc("Rank map size: " + rankMap.size());
        return rankMap;
    }

    /**
     * Performs the crowding-distance-assignment procedure as described on the bottom of page 185.
     *
     * @param population The population for which the crowding distance should be assigned.
     * @param fitnessFunctions The list of objective (fitness) functions.
     * @return Returns a mapping of chromosomes to its crowding distance values.
     */
    public Map<IChromosome<T>, Double> crowdingDistanceAssignment(List<IChromosome<T>> population,
                                                                 List<IFitnessFunction<T>> fitnessFunctions) {

        List<IChromosome<T>> solutions = new LinkedList<>(population);
        int length = population.size();

        Map<IChromosome<T>, Double> crowdingDistanceAssignments = new HashMap<>();

        // init crowding distances
        for (IChromosome<T> solution : solutions) {
            crowdingDistanceAssignments.put(solution, 0.0);
        }

        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {

            // TODO: shuffle solutions to allow a fair sorting of duplicates

            // sort in ascending order of magnitude
            GAUtils.sortByFitness(solutions, fitnessFunction);

            // set boundary solutions to infinity (these solutions will be always included)
            // TODO: consider all boundary points (there might be multiple...)
            crowdingDistanceAssignments.put(solutions.get(0), Double.POSITIVE_INFINITY);
            crowdingDistanceAssignments.put(solutions.get(length - 1), Double.POSITIVE_INFINITY);

            // assign crowding distance to every other solution
            for (int i = 1; i < length - 2; i++) {

                double predecessorFitness = fitnessFunction.getNormalizedFitness(solutions.get(i - 1));
                double successorFitness = fitnessFunction.getNormalizedFitness(solutions.get(i + 1));

                if (!fitnessFunction.isMaximizing()) {
                    // flip fitness values
                    predecessorFitness = 1 - predecessorFitness;
                    successorFitness = 1 - successorFitness;
                }

                IChromosome<T> solution = solutions.get(i);

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

    public List<IChromosome<T>> getGenerationSurvivors2() {
        List<IChromosome<T>> survivors = new ArrayList<>(population);
        final Map<IChromosome<T>, Integer> rankMap = new HashMap<>();
        final Map<IChromosome<T>, Double> crowdingDistanceMap = new HashMap<>();

        List<IChromosome<T>> remaining = new ArrayList<>(population);

        int rank = 0;

        while (!remaining.isEmpty()) {
            List<IChromosome<T>> paretoFront = getParetoFront(remaining, fitnessFunctions);

            for (IChromosome<T> chromosome : paretoFront) {
                remaining.remove(chromosome);
                rankMap.put(chromosome, rank);
            }

            updateCrowdingDistance(paretoFront, fitnessFunctions, crowdingDistanceMap);
            rank++;
        }
        Collections.sort(survivors, new RankComparator<>(rankMap, crowdingDistanceMap));
        return survivors.subList(0, populationSize);
    }

    static class RankComparator<T> implements Comparator<IChromosome<T>> {
        private final Map<IChromosome<T>, Integer> rankMap;
        private final Map<IChromosome<T>, Double> crowdingDistanceMap;

        RankComparator(Map<IChromosome<T>, Integer> rankMap, Map<IChromosome<T>, Double> crowdingDistanceMap) {
            this.rankMap = rankMap;
            this.crowdingDistanceMap = crowdingDistanceMap;
        }

        @Override
        public int compare(IChromosome<T> o1, IChromosome<T> o2) {
            final Integer o1Rank = rankMap.get(o1);
            if (o1Rank == null) {
                throw new IllegalStateException("Rank value not in rank map");
            }
            final Integer o2Rank = rankMap.get(o2);
            if (o2Rank == null) {
                throw new IllegalStateException("Rank value not in rank map");
            }

            int c = o1Rank.compareTo(o2Rank);
            if (c == 0) {
                final Double o1CrowdDistance = crowdingDistanceMap.get(o1);
                if (o1CrowdDistance == null) {
                    throw new IllegalStateException("Crowding distance not in crowding distance map");
                }
                final Double o2CrowdDistance = crowdingDistanceMap.get(o2);
                if (o2CrowdDistance == null) {
                    throw new IllegalStateException("Crowding distance not in crowding distance map");
                }

                if (isEpsEq(o1CrowdDistance, o2CrowdDistance)) {
                    return 0;
                }
                return 0 - o1CrowdDistance.compareTo(o2CrowdDistance);
            }
            return c;
        }
    }
}
