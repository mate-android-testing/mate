package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;

import java.util.List;

/**
 * Provides an implementation of a random walk. In the random walk algorithm an initially created
 * chromosome is iteratively mutated until the termination condition is met.
 *
 * @param <T> The type of the chromosome.
 */
public class RandomWalk<T> extends GeneticAlgorithm<T> {

    /**
     * Initialises the random walk with all necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param mutationFunctions The used mutation function.
     * @param fitnessFunctions The list of fitness functions.
     * @param terminationCondition The used termination condition.
     */
    public RandomWalk(IChromosomeFactory<T> chromosomeFactory,
                      List<IMutationFunction<T>> mutationFunctions,
                      List<IFitnessFunction<T>> fitnessFunctions,
                      ITerminationCondition terminationCondition) {
        super(
                chromosomeFactory,
                null,
                null,
                mutationFunctions,
                fitnessFunctions,
                terminationCondition,
                1,
                1,
                0,
                1);
    }

    /**
     * The single chromosome of the population is iteratively mutated.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Creating generation #" + (currentGenerationNumber + 1));

        IChromosome<T> chromosome = population.get(0);
        IChromosome<T> mutant = singleMutationFunction.mutate(chromosome);

        // keep solely the mutant (offspring)
        population.clear();
        population.add(mutant);

        logCurrentFitness();
        currentGenerationNumber++;
    }
}
