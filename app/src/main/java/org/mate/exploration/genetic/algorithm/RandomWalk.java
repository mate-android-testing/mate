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
 * Provides a generic implementation of a random walk.
 *
 * @param <T> The type of the chromosome.
 */
public class RandomWalk<T> extends GeneticAlgorithm<T> {

    /**
     * Initializes the random walk with all necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param mutationFunction The used mutation function.
     * @param fitnessFunctions The list of fitness functions.
     * @param terminationCondition The used termination condition.
     */
    public RandomWalk(IChromosomeFactory<T> chromosomeFactory, IMutationFunction<T> mutationFunction,
                      List<IFitnessFunction<T>> fitnessFunctions, ITerminationCondition terminationCondition) {
        super(
                chromosomeFactory,
                null,
                null,
                mutationFunction,
                fitnessFunctions,
                terminationCondition,
                1,
                1,
                0,
                1);
    }

    /**
     * A random walk initially samples a random chromosome that is iteratively mutated.
     */
    @Override
    public void createInitialPopulation() {
        MATE.log_acc("Creating generation #" + (currentGenerationNumber + 1));
        IChromosome<T> chromosome = chromosomeFactory.createChromosome();
        population.add(chromosome);
        logCurrentFitness();
        currentGenerationNumber++;
    }

    /**
     * Mutates the chromosome from the previous generation.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Creating generation #" + (currentGenerationNumber + 1));

        IChromosome<T> chromosome = population.get(0);
        IChromosome<T> mutant = mutationFunction.mutate(chromosome);

        // keep solely the mutant (offspring)
        population.clear();
        population.add(mutant);

        logCurrentFitness();
        currentGenerationNumber++;
    }
}
