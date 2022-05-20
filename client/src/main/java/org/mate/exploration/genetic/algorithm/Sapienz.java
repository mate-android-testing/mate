package org.mate.exploration.genetic.algorithm;

import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GAUtils;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides an implementation of the Sapienz approach as described in the paper
 * "Sapienz: Multi-objective Automated Testing for Android Applications". For more details see:
 * http://www0.cs.ucl.ac.uk/staff/K.Mao/archive/p_issta16_sapienz.pdf
 *
 * @param <T> The type of the chromosomes.
 */
public class Sapienz<T> extends GeneticAlgorithm<T> {

    /**
     * Initializes Sapienz with the relevant attributes.
     *
     * @param chromosomeFactory The used chromosome factory, see {@link IChromosomeFactory}.
     * @param selectionFunction The used selection function, see {@link ISelectionFunction}.
     * @param crossOverFunction The used crossover function, see {@link ICrossOverFunction}.
     * @param mutationFunction The used mutation function, see {@link IMutationFunction}.
     * @param fitnessFunctions The used fitness function, see {@link IFitnessFunction}.
     * @param terminationCondition The used termination condition, see {@link ITerminationCondition}.
     * @param populationSize The population size n.
     * @param bigPopulationSize The big population size.
     * @param pCrossover The probability for crossover p.
     * @param pMutate The probability for mutation q.
     */
    public Sapienz(IChromosomeFactory<T> chromosomeFactory,
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
                fitnessFunctions, terminationCondition, populationSize, bigPopulationSize,
                pCrossover, pMutate);
    }

    /**
     * Sapienz follows to a large degree the procedure of the {@link NSGAII} algorithm. Only
     * the {@link ISelectionFunction} differs and a special variation (mutation + crossover) operator
     * is used. For more details see Algorithm 2 of the Sapienz paper.
     */
    @Override
    public void evolve() {

        MATELog.log_acc("Creating population #" + (currentGenerationNumber + 1));
        List<IChromosome<T>> newGeneration = new ArrayList<>(population);

        while (newGeneration.size() < bigPopulationSize) {

            double rnd = Randomness.getRnd().nextDouble();

            if (rnd < pCrossover) { // if r < p (apply crossover)

                /*
                * Sapienz uses a uniform crossover operator that takes two individuals x1, x2 as input
                * and returns two individuals x1', x2'. The first individual x1' is added to the
                * offspring population Q. Since x2' is not used further more, we can stick to the
                * default crossover operators already implemented in MATE. Note that the crossover
                * function directly executes the offspring x1' if necessary.
                 */
                List<IChromosome<T>> parents = selectionFunction.select(population, fitnessFunctions);
                IChromosome<T> offspring = crossOverFunction.cross(parents).get(0);
                newGeneration.add(offspring);
            } else if (rnd < pCrossover + pMutate) { // if r < p + q (apply mutation)

                /*
                * Sapienz mutates a randomly selected individual x1 in a multi-step mutation
                * procedure and adds the mutated individual x1' to the offspring population Q.
                * Note that the mutation function directly executes the mutated individual x1'.
                 */
                List<IChromosome<T>> parents = selectionFunction.select(population, fitnessFunctions);
                IChromosome<T> offspring = mutationFunction.mutate(parents.get(0));
                newGeneration.add(offspring);
            } else { // (apply reproduction)
                List<IChromosome<T>> parents = selectionFunction.select(population, fitnessFunctions);
                newGeneration.add(parents.get(0));
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
     * Retrieves the survivors for the next generation. In Sapienz the new population is filled
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
         * only N solutions can be part of the next generation. Sapienz sorts the population based
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
