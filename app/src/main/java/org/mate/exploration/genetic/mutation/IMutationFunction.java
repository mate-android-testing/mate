package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.chromosome.IChromosome;

import java.util.List;

/**
 * Interface for performing a mutation on a {@link IChromosome} used by {@link IGeneticAlgorithm}
 * and {@link GeneticAlgorithm} respectively
 * @param <T> Type wrapped by the chromosome implementation
 */
public interface IMutationFunction<T> {
    /**
     * Mutate the chromosome
     * @param chromosome chromosome used for the mutation
     * @return resulting mutated chromosome
     */
    List<IChromosome<T>> mutate(IChromosome<T> chromosome);
}
