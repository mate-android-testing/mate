package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.exploration.genetic.util.eda.IProbabilisticModel;

import java.util.List;

/**
 * A generic exploration using an estimation of distribution algorithm (EDA).
 *
 * @param <T> Refers to the type of chromosome.
 */
public class EDA<T> extends GeneticAlgorithm<T> {

    /**
     * The probabilistic model encoding information about the current population.
     */
    private final IProbabilisticModel<T> probabilisticModel;

    /**
     * Initialises the estimation of distribution algorithm with the necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param fitnessFunctions The list of fitness functions.
     * @param terminationCondition The used termination condition.
     * @param populationSize The population size.
     */
    public EDA(final IChromosomeFactory<T> chromosomeFactory,
               final List<IFitnessFunction<T>> fitnessFunctions,
               final ITerminationCondition terminationCondition,
               final int populationSize,
               final IProbabilisticModel<T> probabilisticModel) {
        super(chromosomeFactory, null, null, null,
                fitnessFunctions, terminationCondition, populationSize,
                populationSize, 0.0, 0.0);
        this.probabilisticModel = probabilisticModel;
    }

    /**
     * Creates the initial population and updates the probabilistic model.
     */
    @Override
    public void createInitialPopulation() {

        MATE.log_acc("Creating initial population (1st generation)");

        for (int i = 0; i < populationSize; i++) {
            population.add(chromosomeFactory.createChromosome());
        }

        probabilisticModel.update(population);
        logCurrentFitness();
        currentGenerationNumber++;
    }

    /**
     * Creates a new population and updates the probabilistic model.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));

        population.clear();

        for (int i = 0; i < populationSize; i++) {
            population.add(chromosomeFactory.createChromosome());
        }

        probabilisticModel.update(population);
        logCurrentFitness();
        currentGenerationNumber++;
    }
}
