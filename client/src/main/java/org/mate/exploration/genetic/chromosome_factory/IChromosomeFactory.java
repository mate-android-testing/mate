package org.mate.exploration.genetic.chromosome_factory;

import org.mate.exploration.genetic.chromosome.IChromosome;

/**
 * Interface for generating new {@link IChromosome}s.
 *
 * @param <T> The type of the chromosomes.
 */
public interface IChromosomeFactory<T> {

    /**
     * Generates a new chromosome.
     *
     * @return Returns the newly generated chromosome.
     */
    IChromosome<T> createChromosome();
}
