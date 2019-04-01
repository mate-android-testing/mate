package org.mate.exploration.genetic.core;

import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;

import java.util.List;

public class GenericGeneticAlgorithm<T> extends GeneticAlgorithm<T> {
    public static final String ALGORITHM_NAME = "GenericGeneticAlgorithm";

    /**
     * Initializing the genetic algorithm with all necessary attributes
     *
     * @param chromosomeFactory       see {@link IChromosomeFactory}
     * @param selectionFunction       see {@link ISelectionFunction}
     * @param crossOverFunction       see {@link ICrossOverFunction}
     * @param mutationFunction        see {@link IMutationFunction}
     * @param iFitnessFunctions       see {@link IFitnessFunction}
     * @param terminationCondition    see {@link ITerminationCondition}
     * @param populationSize          size of population kept by the genetic algorithm
     * @param generationSurvivorCount amount of survivors of each generation
     * @param pCrossover              probability that crossover occurs (between 0 and 1)
     * @param pMutate                 probability that mutation occurs (between 0 and 1)
     */
    public GenericGeneticAlgorithm(IChromosomeFactory<T> chromosomeFactory, ISelectionFunction<T> selectionFunction, ICrossOverFunction<T> crossOverFunction, IMutationFunction<T> mutationFunction, List<IFitnessFunction<T>> iFitnessFunctions, ITerminationCondition terminationCondition, int populationSize, int generationSurvivorCount, double pCrossover, double pMutate) {
        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction, iFitnessFunctions, terminationCondition, populationSize, generationSurvivorCount, pCrossover, pMutate);
    }
}
