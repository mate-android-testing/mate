package org.mate.exploration.genetic.algorithm;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.IdSelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;

import java.util.Arrays;
import java.util.List;

public class RandomWalk<T> extends GeneticAlgorithm<T> {
    public static final String ALGORITHM_NAME = "RandomWalk";

    /**
     * Initializing the genetic algorithm with all necessary attributes
     *
     * @param chromosomeFactory    see {@link IChromosomeFactory}
     * @param mutationFunction     see {@link IMutationFunction}
     * @param fitnessFunctions see {@link IFitnessFunction}
     * @param terminationCondition see {@link ITerminationCondition}
     */
    public RandomWalk(IChromosomeFactory<T> chromosomeFactory, IMutationFunction<T> mutationFunction, List<IFitnessFunction<T>> fitnessFunctions, ITerminationCondition terminationCondition) {
        super(chromosomeFactory, new IdSelectionFunction<T>(), null, mutationFunction, fitnessFunctions, terminationCondition, 1, 2, 0, 1);
    }

    /**
     * Always return the new chromosome (offspring)
     * @return new chromosome (offspring) only
     */
    public List<IChromosome<T>> getGenerationSurvivors() {
        return Arrays.asList(population.get(population.size() - 1));
    }
}
