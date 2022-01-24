package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;

/**
 * Interface for performing a mutation on a {@link IChromosome} used by {@link IGeneticAlgorithm}
 * and {@link GeneticAlgorithm} respectively.
 *
 * @param <T> The type wrapped by the chromosomes.
 */
public interface IMutationFunction<T> {

    /**
     * Performs a mutation on the given chromosome.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    IChromosome<T> mutate(IChromosome<T> chromosome);
}
