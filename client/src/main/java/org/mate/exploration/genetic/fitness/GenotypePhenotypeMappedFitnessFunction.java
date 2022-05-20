package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.util.ge.IGenotypePhenotypeMapping;

/**
 * Provides a fitness function for grammatical evolution testing strategies, i.e. where a
 * genotype-phenotype mapping is employed.
 *
 * @param <S> The genotype generic type.
 * @param <T> The phenotype generic type.
 */
public class GenotypePhenotypeMappedFitnessFunction<S, T> implements IFitnessFunction<S> {

    /**
     * Provides the mapping from genotype to phenotype.
     */
    private final IGenotypePhenotypeMapping<S, T> genotypePhenotypeMapping;

    /**
     * The fitness function that can applied on the phenotype.
     */
    private final IFitnessFunction<T> phenotypeFitnessFunction;

    /**
     * Initialises the fitness function with the given genotype to phenotype mapping and the
     * fitness function that is applicable on the phenotype.
     *
     * @param genotypePhenotypeMapping Provides the mapping from genotype to phenotype.
     * @param phenotypeFitnessFunction The fitness function that can be applied on the phenotype.
     */
    public GenotypePhenotypeMappedFitnessFunction(IGenotypePhenotypeMapping<S, T> genotypePhenotypeMapping,
                                                  IFitnessFunction<T> phenotypeFitnessFunction) {
        this.genotypePhenotypeMapping = genotypePhenotypeMapping;
        this.phenotypeFitnessFunction = phenotypeFitnessFunction;
    }

    /**
     * Computes the fitness value for the given chromosome, i.e. the genotype is first mapped to
     * the phenotype and then the fitness is evaluated.
     *
     * @param genotypeChromosome The chromosome for which the fitness should be evaluated.
     * @return Returns the fitness value for the given chromosome.
     */
    @Override
    public double getFitness(IChromosome<S> genotypeChromosome) {
        return phenotypeFitnessFunction.getFitness(genotypePhenotypeMapping.map(genotypeChromosome));
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} if the underlying fitness function applied on the phenotype is
     *          maximising, otherwise {@code false} is returned.
     */
    @Override
    public boolean isMaximizing() {
        return phenotypeFitnessFunction.isMaximizing();
    }

    /**
     * Returns the normalised fitness value of the given chromosome, i.e. the genotype is first
     * mapped to the phenotype and then the fitness is evaluated.
     *
     * @param genotypeChromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<S> genotypeChromosome) {
        return phenotypeFitnessFunction.getNormalizedFitness(genotypePhenotypeMapping.map(genotypeChromosome));
    }

    /**
     * Returns the phenotype for the given genotype chromosome.
     *
     * @param genotypeChromosome The given genotype chromosome.
     * @return Returns the genotype chromosome.
     */
    public IChromosome<T> getPhenoType(IChromosome<S> genotypeChromosome) {
        return genotypePhenotypeMapping.map(genotypeChromosome);
    }
}
