package org.mate.exploration.genetic;

interface IChromosome<T> {
    T getValue();

    double getFitness();

    String getId();
}
