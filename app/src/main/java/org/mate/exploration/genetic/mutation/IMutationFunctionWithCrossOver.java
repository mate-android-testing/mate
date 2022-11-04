package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;

public interface IMutationFunctionWithCrossOver<T> extends IMutationFunction<T> {

    /**
     * Performs a mutation on the given chromosome with a.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    IChromosome<T> mutate(IChromosome<T> chromosome, ICrossOverFunction<T> crossOverFunction);
}
