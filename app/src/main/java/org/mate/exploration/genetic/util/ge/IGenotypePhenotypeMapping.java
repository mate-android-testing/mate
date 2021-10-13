package org.mate.exploration.genetic.util.ge;

import org.mate.exploration.genetic.chromosome.IChromosome;

/**
 * A mapping from a genotype to a phenotype.
 * @param <S>
 * @param <T>
 */
public interface IGenotypePhenotypeMapping<S, T> {
    /**
     * Generate or retrieve the phenotype for the given genotype
     */
    IChromosome<T> map(IChromosome<S> genotype);
}
