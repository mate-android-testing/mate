package org.mate.exploration.genetic.algorithm;

import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;

import java.util.List;

/**
 * Provides an implementation of a standard genetic algorithm.
 *
 * @param <T> The type of the chromosomes.
 */
public class StandardGeneticAlgorithm<T> extends GeneticAlgorithm<T> {

    /**
     * Initialises the standard genetic algorithm with the necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param selectionFunction The used selection function.
     * @param crossOverFunction The used crossover function.
     * @param mutationFunction The used mutation function.
     * @param fitnessFunctions The list of fitness functions. Only a single fitness function is used here.
     * @param terminationCondition The used termination condition.
     * @param populationSize The population size.
     * @param bigPopulationSize The big population size.
     * @param pCrossover The probability rate for crossover.
     * @param pMutate The probability rate for mutation.
     */
    public StandardGeneticAlgorithm(IChromosomeFactory<T> chromosomeFactory,
                                    ISelectionFunction<T> selectionFunction,
                                    ICrossOverFunction<T> crossOverFunction,
                                    IMutationFunction<T> mutationFunction,
                                    List<IFitnessFunction<T>> fitnessFunctions,
                                    ITerminationCondition terminationCondition,
                                    int populationSize,
                                    int bigPopulationSize,
                                    double pCrossover,
                                    double pMutate) {
        super(chromosomeFactory,
                selectionFunction,
                crossOverFunction,
                mutationFunction,
                fitnessFunctions,
                terminationCondition,
                populationSize,
                bigPopulationSize,
                pCrossover,
                pMutate);
    }
}
