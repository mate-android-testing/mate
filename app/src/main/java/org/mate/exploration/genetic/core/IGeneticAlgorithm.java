package org.mate.exploration.genetic.core;

import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome.IChromosome;

import java.util.List;

/**
 * Defines the interface for any genetic algorithm.
 *
 * @param <T> The type of the chromosomes.
 */
public interface IGeneticAlgorithm<T> extends Algorithm {

    /**
     * Gets the current population.
     *
     * @return Returns the current population.
     */
    List<IChromosome<T>> getCurrentPopulation();

    /**
     * Creates the initial population.
     */
    void createInitialPopulation();

    /**
     * Performs a single step of evolution.
     */
    void evolve();

    /**
     * Defines the entry point of the genetic algorithm.
     */
    void run();

    /**
     * Determines the survivors of the current generation.
     *
     * @return Returns the survivors of the current generation.
     */
    List<IChromosome<T>> getGenerationSurvivors();
}
