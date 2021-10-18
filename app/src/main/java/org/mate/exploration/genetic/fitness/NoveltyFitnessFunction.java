package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

import java.util.List;

/**
 * Provides a fitness metric based on the novelty/diversity of a chromosome. This requires that the
 * AUT has been instrumented with the respective coverage module.
 *
 * @param <T> The type of the chromosome.
 */
public class NoveltyFitnessFunction<T> implements IFitnessFunction<T> {

    /**
     * The underlying objectives, e.g. branches.
     */
    private final String objectives;

    /**
     * Initialises the novelty fitness function with the given objectives type.
     *
     * @param objectives The type of objectives, e.g. branches.
     */
    public NoveltyFitnessFunction(String objectives) {
        this.objectives = objectives;
    }

    /**
     * The novelty fitness value can't be computed solely based on the given chromosome. Do not
     * call this method, see {@link #getFitness(List, List, int)} for more information.
     *
     * @param chromosome The given chromosome.
     * @return Returns an {@link UnsupportedOperationException}.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    /**
     * The novelty fitness value can't be computed solely based on the given chromosome. Do not
     * call this method, see {@link #getFitness(List, List, int)} for more information.
     *
     * @param chromosome The given chromosome.
     * @return Returns an {@link UnsupportedOperationException}.
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    /**
     * Returns whether this fitness function is maximising or minimising.
     *
     * @return Returns {@code true} since we aim for maximising novelty.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Computes the novelty vector for the chromosomes contained in the either the population
     * or the archive.
     *
     * @param population The chromosomes in the current population.
     * @param archive The chromosomes in the current archive.
     * @param nearestNeighbours The number of nearest neighbours k.
     * @return Returns the novelty vector.
     */
    public List<Double> getFitness(List<IChromosome<T>> population,
                             List<IChromosome<T>> archive, int nearestNeighbours) {
        return FitnessUtils.getNoveltyVector(population, archive, nearestNeighbours, objectives);
    }
}
