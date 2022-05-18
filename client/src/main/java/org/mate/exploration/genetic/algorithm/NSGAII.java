package org.mate.exploration.genetic.algorithm;

import org.mate.commons.utils.MATELog;
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
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
     * NSGA-II follows the same procedure as in a {@link StandardGeneticAlgorithm} except the
     * additional computation of the pareto fronts and the crowding distance measurement.
     */
    @Override
    public void evolve() {

        MATELog.log_acc("Creating population #" + (currentGenerationNumber + 1));
        List<IChromosome<T>> newGeneration = new ArrayList<>(population);

        /*
        * We only need to compute the fronts F (= F1, F2,...) and the crowding distances once
        * for the selection function.
         */
        Map<Integer, List<IChromosome<T>>> paretoFronts
                = GAUtils.fastNonDominatedSort(population, fitnessFunctions);
        Map<IChromosome<T>, Integer> rankMap = GAUtils.getRankMap(paretoFronts);
        Map<IChromosome<T>, Double> crowdingDistanceMap
                = GAUtils.crowdingDistanceAssignment(population, fitnessFunctions);

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
        Map<Integer, List<IChromosome<T>>> paretoFronts
                = GAUtils.fastNonDominatedSort(population, fitnessFunctions);
        int i = 1;

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
}
