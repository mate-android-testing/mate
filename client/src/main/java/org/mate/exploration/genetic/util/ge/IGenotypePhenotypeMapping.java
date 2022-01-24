package org.mate.exploration.genetic.util.ge;

import org.mate.exploration.genetic.chromosome.IChromosome;

/**
 * Provides the interface for the mapping procedure from a genotype to a phenotype.
 *
 * @param <S> The genotype generic type.
 * @param <T> The phenotype generic type.
 */
public interface IGenotypePhenotypeMapping<S, T> {

    /**
     * Maps the given genotype to the corresponding phenotype.
     */
    IChromosome<T> map(IChromosome<S> genotype);
}
