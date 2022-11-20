package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GAUtils;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.Randomness;

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
     * The outer cross over function.
     * Typically, it is an uniform cross over function.
     */
    private final ICrossOverFunction<T> uniformCrossOver;

    /**
     * The cross over function used in the mutation step of the Sapienz algorithm.
     * Typically, it is an one point cross over function.
     */
    private final ICrossOverFunction<TestCase> onePointCrossOver;

    /**
     * Initializes Sapienz with the relevant attributes.
     *
     * @param chromosomeFactory The used chromosome factory, see {@link IChromosomeFactory}.
     * @param selectionFunction The used selection function, see {@link ISelectionFunction}.
     * @param crossOverFunctions The used crossover function, see {@link ICrossOverFunction}.
     * @param mutationFunctions The used mutation function, see {@link IMutationFunction}.
     * @param fitnessFunctions The used fitness function, see {@link IFitnessFunction}.
     * @param terminationCondition The used termination condition, see {@link ITerminationCondition}.
     * @param populationSize The population size n.
     * @param bigPopulationSize The big population size.
     * @param pCrossover The probability for crossover p.
     * @param pMutate The probability for mutation q.
     */
    public Sapienz(IChromosomeFactory<T> chromosomeFactory,
                   ISelectionFunction<T> selectionFunction,
                   List<ICrossOverFunction<T>> crossOverFunctions,
                   List<IMutationFunction<T>> mutationFunctions,
                   List<IFitnessFunction<T>> fitnessFunctions,
                   ITerminationCondition terminationCondition,
                   int populationSize,
                   int bigPopulationSize,
                   double pCrossover,
                   double pMutate) {
        super(chromosomeFactory, selectionFunction, crossOverFunctions, mutationFunctions,
                fitnessFunctions, terminationCondition, populationSize, bigPopulationSize,
                pCrossover, pMutate);

        if (crossOverFunctions.size() == 2) {
            uniformCrossOver = crossOverFunctions.get(0);
            onePointCrossOver = (ICrossOverFunction<TestCase>) crossOverFunctions.get(1);
        } else {
            throw new IllegalArgumentException("Sapienz needs two cross over functions! "
                    + "An uniform cross over function and the one point cross over function.");
        }
    }

    /**
     * Sapienz follows to a large degree the procedure of the {@link NSGAII} algorithm. Only
     * the {@link ISelectionFunction} differs and a special variation (mutation + crossover) operator
     * is used. For more details see Algorithm 2 of the Sapienz paper.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));
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

                IChromosome<T> offspring = uniformCrossOver.cross(parents).get(0);
                newGeneration.add(offspring);
            } else if (rnd < pCrossover + pMutate) { // if r < p + q (apply mutation)

                /*
                * Sapienz mutates a randomly selected individual x1 in a multi-step mutation
                * procedure and adds the mutated individual x1' to the offspring population Q.
                * Note that the mutation function directly executes the mutated individual x1'.
                 */
                List<IChromosome<T>> parents = selectionFunction.select(population, fitnessFunctions);
                IChromosome<T> parent = parents.get(0);
                T object = parent.getValue();

                if (object instanceof TestSuite) {
                    parent = produceTestCaseMutation((TestSuite) object);
                }

                IChromosome<T> offspring = singleMutationFunction.mutate(parent);

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

    private IChromosome<T> produceTestCaseMutation(TestSuite testSuite) {
        List<TestCase> testCases = new ArrayList<>(testSuite.getTestCases());

        // TODO: Not mutated
        // shuffle the test cases within the test suite
        Randomness.shuffleList(testCases);

        for (int i = 1; i < testCases.size(); i = i + 2) {
            double rnd = Randomness.getRnd().nextDouble();

            if (rnd < pMutate) { // if r < q
                /*
                 * Sapienz performs a one-point crossover on two neighbouring test cases. Since
                 * MATE only supports crossover functions that return a single offspring, we make
                 * the one-point crossover here in place.
                 */
                TestCase t1 = testCases.get(i - 1);
                TestCase t2 = testCases.get(i);
                IChromosome<TestCase> t1Chromosome = new Chromosome<>(t1);
                IChromosome<TestCase> t2Chromosome = new Chromosome<>(t2);
                List<IChromosome<TestCase>> t1AndT2 = new ArrayList<>();
                t1AndT2.add(t1Chromosome);
                t1AndT2.add(t2Chromosome);

                List<IChromosome<TestCase>> result = onePointCrossOver.cross(t1AndT2);

                TestCase t1Result = result.get(0).getValue();
                TestCase t2Result = result.get(1).getValue();

                testCases.set((i - 1), t1Result);
                testCases.set(i, t2Result);
            }
        }

        TestSuite suite = new TestSuite();
        suite.getTestCases().addAll(testCases);

        return new Chromosome<>((T) suite);
    }
}
