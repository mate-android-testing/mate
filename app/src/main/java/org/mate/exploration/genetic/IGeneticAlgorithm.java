package org.mate.exploration.genetic;

import java.util.List;

public interface IGeneticAlgorithm<T> {
    List<IChromosome<T>> getCurrentPopulation();

    void createInitialPopulation();

    void evolve();

    void run();
}
