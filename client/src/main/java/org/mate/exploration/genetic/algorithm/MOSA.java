package org.mate.exploration.genetic.algorithm;

import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Randomness;
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
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.FitnessUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the Many-Objective Sorting Algorithm (MOSA) based on the paper:
 * "Reformulating Branch Coverage as a Many-Objective Optimization Problem", see for more details:
 * https://ieeexplore.ieee.org/abstract/document/7102604
 *
 * @param <T> The type of the chromosomes.
 */
public class MOSA<T> extends GeneticAlgorithm<T> {

    /**
     * An archive that keeps track of the best chromosomes for each target.
     */
    private final Map<IFitnessFunction<T>, IChromosome<T>> archive = new HashMap<>();

    /**
     * The fitness functions or targets that haven't been covered yet.
     */
    private final List<IFitnessFunction<T>> uncoveredFitnessFunctions = new ArrayList<>();

    /**
     * MOSA uses a customized selection function that considers both the rank and the crowding
     * distance for the selection as in NSGA-II.
     */
    private final CrowdedTournamentSelectionFunction<T> selectionFunction;

    /**
     * Initialises the MOSA algorithm with the necessary attributes.
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
    public MOSA(IChromosomeFactory<T> chromosomeFactory,
                ISelectionFunction<T> selectionFunction,
                ICrossOverFunction<T> crossOverFunction,
                IMutationFunction<T> mutationFunction,
                List<IFitnessFunction<T>> fitnessFunctions,
                ITerminationCondition terminationCondition,
                int populationSize,
                int bigPopulationSize,
                double pCrossover,
                double pMutate) {
        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction, fitnessFunctions,
                terminationCondition, populationSize, bigPopulationSize, pCrossover, pMutate);

        uncoveredFitnessFunctions.addAll(fitnessFunctions);
        this.selectionFunction = (CrowdedTournamentSelectionFunction<T>) selectionFunction;
    }

    /**
     * MOSA generates a random population P_t and updates the archive accordingly, see line 3 and 4
     * of Algorithm 1. In addition, we need to update which fitness functions (targets) have been
     * covered.
     */
    @Override
    public void createInitialPopulation() {

        super.createInitialPopulation();
        updateArchive(population);

        // we need to filter the covered fitness functions (targets)
        Set<IFitnessFunction<T>> coveredFitnessFunctions = getCoveredFitnessFunctions(
                uncoveredFitnessFunctions, population);
        uncoveredFitnessFunctions.removeAll(coveredFitnessFunctions);
    }

    /**
     * Refers to the lines 5 to 18 of Algorithm 1.
     */
    @Override
    public void evolve() {

        MATELog.log_acc("Creating population #" + (currentGenerationNumber + 1));
        List<IChromosome<T>> newGeneration = new ArrayList<>(population);

        /*
         * We only need to compute the fronts F (= F1, F2,...) and the crowding distances once
         * for the selection function. NOTE: We use here a preferenceSorting instead of the basic
         * fastNonDominatedSort and assign the crowding distances based on the uncovered fitness
         * functions (targets).
         */
        Map<Integer, List<IChromosome<T>>> paretoFronts = preferenceSorting(population);
        Map<IChromosome<T>, Integer> rankMap = GAUtils.getRankMap(paretoFronts);
        Map<IChromosome<T>, Double> crowdingDistanceMap
                = GAUtils.crowdingDistanceAssignment(population, uncoveredFitnessFunctions);

        while (newGeneration.size() < bigPopulationSize) {

            // performs a binary tournament selection that considers both rank and crowding distance
            List<IChromosome<T>> parents = selectionFunction.select(population, rankMap, crowdingDistanceMap);

            List<IChromosome<T>> offsprings;

            if (Randomness.getRnd().nextDouble() < pCrossover) {
                offsprings = crossOverFunction.cross(parents);
            } else {
                offsprings = parents;
            }

            for (IChromosome<T> offspring : offsprings) {

                if (Randomness.getRnd().nextDouble() < pMutate) {
                    offspring = mutationFunction.mutate(offspring);
                }

                if (newGeneration.size() < bigPopulationSize) {
                    newGeneration.add(offspring);
                } else {
                    // big population size reached -> early abort
                    break;
                }
            }
        }

        // we need to filter the covered fitness functions (targets)
        Set<IFitnessFunction<T>> coveredFitnessFunctions = getCoveredFitnessFunctions(
                uncoveredFitnessFunctions, newGeneration);
        uncoveredFitnessFunctions.removeAll(coveredFitnessFunctions);

        population.clear();
        population.addAll(newGeneration);
        List<IChromosome<T>> survivors = getGenerationSurvivors(); // line 8 onwards
        population.clear();
        population.addAll(survivors);
        List<IChromosome<T>> chromosomes = new ArrayList<>();
        chromosomes.addAll(population);
        chromosomes.addAll(archive.values());
        updateArchive(chromosomes); // line 17
        logCurrentFitness();
        currentGenerationNumber++; // line 18

        // clean the cache from time to time, otherwise we end up with an OOM error for large apps
        FitnessUtils.cleanCache(chromosomes);
    }

    /**
     * Retrieves the covered fitness functions (targets) for the given chromosomes.
     *
     * @param uncoveredFitnessFunctions The list of uncovered fitness functions so far.
     * @param chromosomes The given list of chromosomes.
     * @return Returns the set containing the uncovered fitness functions.
     */
    private Set<IFitnessFunction<T>> getCoveredFitnessFunctions(
            List<IFitnessFunction<T>> uncoveredFitnessFunctions, List<IChromosome<T>> chromosomes) {

        Set<IFitnessFunction<T>> coveredFitnessFunctions = new HashSet<>();

        for (IFitnessFunction<T> fitnessFunction : uncoveredFitnessFunctions) {

            final boolean isMaximising = fitnessFunction.isMaximizing();

            for (IChromosome<T> chromosome : chromosomes) {
                final double fitness = fitnessFunction.getNormalizedFitness(chromosome);
                if (isMaximising ? fitness == 1 : fitness == 0) {
                    coveredFitnessFunctions.add(fitnessFunction);
                    break;
                }
            }
        }

        return coveredFitnessFunctions;
    }

    /**
     * Returns the new population P_t+1, see line 8 to 16 of Algorithm 1.
     *
     * @return Returns the new population P_t+1.
     */
    @Override
    public List<IChromosome<T>> getGenerationSurvivors() {

        List<IChromosome<T>> survivors = new LinkedList<>();

        // fill up the new population P_t+1 with the pareto fronts as in NSGA-II
        Map<Integer, List<IChromosome<T>>> paretoFronts = preferenceSorting(population);
        int i = 0;

        // add solutions until a front can't be fully accommodated
        while (survivors.size() + paretoFronts.get(i).size() < populationSize) {
            survivors.addAll(paretoFronts.get(i));
            i = i + 1;
        }

        // sort last front in descending order of the crowding distance
        List<IChromosome<T>> lastFront = paretoFronts.get(i);
        final Map<IChromosome<T>, Double> crowdingDistanceMap
                = GAUtils.crowdingDistanceAssignment(lastFront, fitnessFunctions);
        lastFront = GAUtils.sortByCrowdingDistance(lastFront, crowdingDistanceMap);

        // fill up the remaining slots with the least crowded chromosomes of the last front
        survivors.addAll(lastFront.subList(0, populationSize - survivors.size()));
        return survivors;
    }

    /**
     * Performs a preference sorting as described in Algorithm 2.
     *
     * @param population The population T.
     * @return Returns the pareto fronts, which is a mapping of a rank to a list of chromosomes
     *          belonging to that rank.
     */
    private Map<Integer, List<IChromosome<T>>> preferenceSorting(List<IChromosome<T>> population) {

        Map<Integer, List<IChromosome<T>>> paretoFronts = new HashMap<>();
        List<IChromosome<T>> candidates = new ArrayList<>(population);
        List<IChromosome<T>> firstParetoFront = new ArrayList<>(); // F_0

        // the first pareto front F_0 consists of the 'best' test cases of the uncovered targets
        for (IFitnessFunction<T> fitnessFunction : uncoveredFitnessFunctions) {

            // sort in descending order -> best chromosomes come first
            candidates = GAUtils.sortByFitnessAndLength(candidates, fitnessFunction);
            Collections.reverse(candidates);
            IChromosome<T> best = candidates.get(0);
            firstParetoFront.add(best);
        }

        paretoFronts.put(0, firstParetoFront);

        // for all remaining test cases in T a fast-non-dominated-sort is used
        candidates.removeAll(firstParetoFront);

        if (!candidates.isEmpty()) {
            // derive the fronts F_1 .. F_d
            paretoFronts.putAll(GAUtils.fastNonDominatedSort(candidates, uncoveredFitnessFunctions));
        }

        return paretoFronts;
    }

    /**
     * Updates the archive as described in Algorithm 3.
     *
     * @param chromosomes The list of chromosomes that might be added to the archive.
     */
    private void updateArchive(List<IChromosome<T>> chromosomes) {

        /*
        * For each fitness function (target) we store the best chromosome in the archive.
        * This is the chromosome with the best fitness values and the shortest length.
         */
        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {

            IChromosome<T> best = archive.get(fitnessFunction); // null if none present yet
            double bestLength = best == null ? Double.POSITIVE_INFINITY : getChromosomeLength(best);

            boolean maximizing = fitnessFunction.isMaximizing();

            for (IChromosome<T> chromosome : chromosomes) {

                final double fitness = fitnessFunction.getNormalizedFitness(chromosome);
                final double length = getChromosomeLength(chromosome);

                if ((maximizing ? fitness == 1 : fitness == 0) && length < bestLength) {
                    archive.put(fitnessFunction, chromosome);
                    bestLength = length;
                }
            }
        }
    }

    /**
     * Determines the length of the given chromosome.
     *
     * @param chromosome The chromosome for which its length should be determined.
     * @return Returns the length of the given chromosome.
     */
    private int getChromosomeLength(IChromosome<T> chromosome) {

        if (chromosome.getValue() instanceof TestCase) {
            return ((TestCase) chromosome.getValue()).getActionSequence().size();
        } else if (chromosome.getValue() instanceof TestSuite) {
            int length = 0;
            for (TestCase testCase : ((TestSuite) chromosome.getValue()).getTestCases()) {
                length += testCase.getActionSequence().size();
            }
            return length;
        } else {
            throw new UnsupportedOperationException("Chromosome type "
                    + chromosome.getValue().getClass() + " not yet supported!");
        }
    }
}
