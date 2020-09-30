package org.mate.exploration.genetic.core;

import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome.IChromosome;

import java.util.List;

/**
 * Interface for genetic algorithms
 * @param <T> Type wrapped by the chromosome implementation
 */
public interface IGeneticAlgorithm<T> extends Algorithm {
    /**
     * Get the current population of the genetic algorithm
     * @return current population
     */
    List<IChromosome<T>> getCurrentPopulation();

    /**
     * Create the initial population for the genetic algorithm
     */
    void createInitialPopulation();

    /**
     * Perform a single step of evolution
     */
    void evolve();

    /**
     * Creates initial population and repeatedly executes evolve until the termination condition is
     * met. Do not manually execute {@link #createInitialPopulation()} before using run, as run will
     * execute it as well.
     */
    void run();

    List<IChromosome<T>> getGenerationSurvivors();
}
