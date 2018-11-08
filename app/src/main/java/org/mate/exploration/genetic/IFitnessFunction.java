package org.mate.exploration.genetic;

public interface IFitnessFunction<T> {
    double getFitness(IChromosome<T> chromosome);
}
