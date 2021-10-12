package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

import java.util.List;

/**
 * Provides a fitness metric based on the novelty/diversity of a chromosome. This requires that the
 * AUT has been instrumented with the method coverage module.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class NoveltyFitnessFunction<T> implements IFitnessFunction<T> {

    /**
     * The novelty fitness value can't be computed solely based on the given chromosome. Do not
     * call this method, see {@link #getFitness(IChromosome, List, List, int)} for more information.
     *
     * @param chromosome The given chromosome.
     * @return Returns an {@link UnsupportedOperationException}.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    /**
     * Computes the novelty of the given chromosome.
     *
     * @param chromosome The given chromosome.
     * @param population The chromosomes in the current population.
     * @param archive The chromosomes in the current archive.
     * @param nearestNeighbours The number of nearest neighbours k.
     * @return Returns the novelty of the given chromosome.
     */
    public double getFitness(IChromosome<T> chromosome, List<IChromosome<T>> population,
                             List<IChromosome<T>> archive, int nearestNeighbours) {
        return FitnessUtils.getNovelty(chromosome, population, archive, nearestNeighbours);
    }
}
