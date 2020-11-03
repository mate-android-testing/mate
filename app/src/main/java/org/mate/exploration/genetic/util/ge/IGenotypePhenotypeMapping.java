package org.mate.exploration.genetic.util.ge;

import org.mate.exploration.genetic.chromosome.IChromosome;

public interface IGenotypePhenotypeMapping<S, T> {
    IChromosome<T> map(IChromosome<S> genotype);
}
