package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.util.ge.IGenotypePhenotypeMapping;

/**
 * Fitness function for genotype-phenotype mapped chromosomes
 * @param <S> Genotype generic type
 * @param <T> Phenotype generic type
 */
public class GenotypePhenotypeMappedFitnessFunction<S, T> implements IFitnessFunction<S> {
    private final IGenotypePhenotypeMapping<S, T> genotypePhenotypeMapping;
    private final IFitnessFunction<T> phenotypeFitnessFunction;

    /**
     * Create the fitness function with the given mapping from genotype to phenotype and the given
     * fitness function for the phenotype which is used to determine the fitness
     * @param genotypePhenotypeMapping mapping from the genotype to the phenotype
     * @param phenotypeFitnessFunction fitness function for the phenotype
     */
    public GenotypePhenotypeMappedFitnessFunction(IGenotypePhenotypeMapping<S, T> genotypePhenotypeMapping, IFitnessFunction<T> phenotypeFitnessFunction) {
        this.genotypePhenotypeMapping = genotypePhenotypeMapping;
        this.phenotypeFitnessFunction = phenotypeFitnessFunction;
    }

    @Override
    public double getFitness(IChromosome<S> genotypeChromosome) {
        return phenotypeFitnessFunction.getFitness(genotypePhenotypeMapping.map(genotypeChromosome));
    }

    @Override
    public boolean isMaximizing() {
        return phenotypeFitnessFunction.isMaximizing();
    }

    @Override
    public double getNormalizedFitness(IChromosome<S> genotypeChromosome) {
        return phenotypeFitnessFunction.getNormalizedFitness(genotypePhenotypeMapping.map(genotypeChromosome));
    }
}
