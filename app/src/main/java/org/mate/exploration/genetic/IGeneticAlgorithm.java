package org.mate.exploration.genetic;

import java.util.Map;

interface IGeneticAlgorithm<T> {
    Map<String, IChromosome<T>> getCurrentPopulation();

    void createInitialPopulation();

    void evolve();

    void run();
}
