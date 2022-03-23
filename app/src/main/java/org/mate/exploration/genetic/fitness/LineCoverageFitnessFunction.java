package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a fitness function that aims to maximise line coverage. This requires that the AUT
 * has been manually instrumented with Jacoco.
 *
 * @param <T> The type wrapped by the chromosomes.
 */
public class LineCoverageFitnessFunction<T> implements IFitnessFunction<T> {

    /**
     * Caches for each chromosome the computed line coverage.
     */
    private final Map<IChromosome<T>, Double> cache;

    /**
     * Initialises the fitness function with an empty cache.
     */
    public LineCoverageFitnessFunction() {
        cache = new HashMap<>();
    }

    /**
     * Retrieves the line coverage for the given chromosome.
     *
     * @param chromosome The chromosome for which we want to retrieve its fitness value.
     * @return Returns the line coverage for the given chromosome.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {
        if (cache.containsKey(chromosome)) {
            return cache.get(chromosome);
        }
        double fitness = FitnessUtils.getFitness(chromosome);
        cache.put(chromosome, fitness);
        return fitness;
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} since this fitness functions aims to maximise the line coverage.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns the normalised fitness value, i.e. the line coverage divided by 100.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome) / 100;
    }
}
