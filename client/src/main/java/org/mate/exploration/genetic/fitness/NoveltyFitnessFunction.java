package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

import java.util.Collections;
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
     * The novelty fitness value can't be computed solely based on the given chromosome. Do not
     * call this method, see {@link #getFitness(IChromosome, List, List, int)} for more information.
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
     * Computes the novelty vector for the given chromosomes. Each novelty score is bounded
     * in [0,1], where a higher novelty score is better.
     *
     * @param chromosomes The chromosomes for which novelty should be evaluated.
     * @param nearestNeighbours The number of nearest neighbours k.
     * @return Returns the novelty vector.
     */
    public List<Double> getFitness(List<IChromosome<T>> chromosomes, int nearestNeighbours) {

        if (chromosomes.size() == 1) {
            /*
            * If there is only a single chromosome, there is no way to compute novelty by comparing
            * the chromosomes with each other, since there is no other chromosome. Thus, we can only
            * assign the best novelty score in this case.
             */
            return Collections.singletonList(1.0);
        } else {
            return FitnessUtils.getNoveltyVector(chromosomes, nearestNeighbours, objectives);
        }
    }

    /**
     * Computes the novelty for the given chromosome. The resulting novelty is bounded in [0,1],
     * where a higher novelty score is better.
     *
     * @param chromosome The chromosome for which the novelty should be evaluated.
     * @param population The current population.
     * @param archive The current archive.
     * @param nearestNeighbours The number of nearest neighbours k.
     * @return Returns the novelty for the given chromosome.
     */
    public double getFitness(IChromosome<T> chromosome, List<IChromosome<T>> population,
                             List<IChromosome<T>> archive, int nearestNeighbours) {

        if (population.isEmpty() && archive.isEmpty()) {
            /*
            * The first chromosome gets the highest assignable novelty value, since there is other
            * chromosome to compare it against.
             */
            return 1.0;
        } else {
            return FitnessUtils.getNovelty(chromosome, population, archive, nearestNeighbours, objectives);
        }
    }
}
