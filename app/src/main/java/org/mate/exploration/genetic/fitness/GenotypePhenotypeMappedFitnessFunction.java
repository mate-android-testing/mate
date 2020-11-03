package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.util.ge.IGenotypePhenotypeMapping;

public class GenotypePhenotypeMappedFitnessFunction<S, T> implements IFitnessFunction<S> {
    private final IGenotypePhenotypeMapping<S, T> genotypePhenotypeMapping;
    private final IFitnessFunction<T> phenotypeFitnessFunction;

    public GenotypePhenotypeMappedFitnessFunction(IGenotypePhenotypeMapping<S, T> genotypePhenotypeMapping, IFitnessFunction<T> phenotypeFitnessFunction) {
        this.genotypePhenotypeMapping = genotypePhenotypeMapping;
        this.phenotypeFitnessFunction = phenotypeFitnessFunction;
    }

    @Override
    public double getFitness(IChromosome<S> genotypeChromosome) {
        return phenotypeFitnessFunction.getFitness(genotypePhenotypeMapping.map(genotypeChromosome));
    }
}
