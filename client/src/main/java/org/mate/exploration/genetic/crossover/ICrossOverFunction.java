package org.mate.exploration.genetic.crossover;

import org.mate.exploration.genetic.chromosome.IChromosome;

import java.util.List;

/**
 * Provides the interface for a crossover function.
 */
public interface ICrossOverFunction<T> {

    /**
     * Performs a crossover on the given parents.
     *
     * @param parents The parents that undergo crossover.
     * @return Returns the generated offsprings.
     */
    List<IChromosome<T>> cross(List<IChromosome<T>> parents);
}
