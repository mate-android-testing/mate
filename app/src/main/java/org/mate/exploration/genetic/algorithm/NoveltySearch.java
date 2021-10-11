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
 * A novelty search implementation following the paper 'A Novelty Search Approach for Automatic Test
 * Data Generation', see https://hal.archives-ouvertes.fr/hal-01121228/document.
 *
 * @param <T> Refers to either a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class NoveltySearch<T> extends GeneticAlgorithm<T> {

    // TODO: model archive

    /**
     * The maximal size of the archive, denoted as L.
     */
    private final int limit;

    /**
     * The novelty threshold T.
     */
    private final double threshold;

    /**
     * The number of nearest neighbours that should be considered, denoted as k.
     */
    private final int nearestNeighbours;

    /**
     * Initializes the genetic algorithm with all the necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param selectionFunction The used selection function.
     * @param crossOverFunction The used crossover function.
     * @param mutationFunction The used mutation function.
     * @param fitnessFunctions The used fitness/novelty function.
     * @param terminationCondition The used termination condition.
     * @param populationSize The population size.
     * @param bigPopulationSize The big population size.
     * @param pCrossover The probability for crossover.
     * @param pMutate The probability for mutation.
     * @param limit The maximal size (L) of the archive.
     * @param threshold The novelty threshold T.
     * @param nearestNeighbours The number of nearest neighbours k.
     */
    public NoveltySearch(IChromosomeFactory<T> chromosomeFactory,
                         ISelectionFunction<T> selectionFunction,
                         ICrossOverFunction<T> crossOverFunction,
                         IMutationFunction<T> mutationFunction,
                         List<IFitnessFunction<T>> fitnessFunctions,
                         ITerminationCondition terminationCondition,
                         int populationSize,
                         int bigPopulationSize,
                         double pCrossover,
                         double pMutate,
                         int nearestNeighbours,
                         int limit,
                         double threshold) {
        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction,
                fitnessFunctions, terminationCondition, populationSize, bigPopulationSize,
                pCrossover, pMutate);
        this.nearestNeighbours = nearestNeighbours;
        this.limit = limit;
        this.threshold = threshold;
    }

    @Override
    public void createInitialPopulation() {
        super.createInitialPopulation();
    }

    @Override
    public void evolve() {
        super.evolve();
    }
}
