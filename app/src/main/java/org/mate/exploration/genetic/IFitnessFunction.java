package org.mate.exploration.genetic;

/**
 * Interface for evaluating the fitness of a {@link IChromosome} used by
 * {@link IGeneticAlgorithm} and {@link GeneticAlgorithm} respectively
 * @param <T> Type wrapped by the chromosome implementation
 */
public interface IFitnessFunction<T> {
    /**
     * Calculate fitness for the chromosome
     * @param chromosome chromosome to calculate fitness for
     * @return fitness of chromosome
     */
    double getFitness(IChromosome<T> chromosome);
}
