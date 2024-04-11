package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.FitnessUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a fitness function for crash reproduction.
 *
 * @param <T> The chromosomes type.
 */
public class CrashDistanceFitnessFunction<T> implements IActionFitnessFunction<T> {

    /**
     * Stores for each chromosome the fitness values on a per action basis.
     */
    private static final Map<IChromosome, List<Float>> actionCache = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {
        return getNormalizedFitness(chromosome);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMaximizing() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        final List<Float> crashDistances = getNormalizedFitnessVector(chromosome);
        return crashDistances.get(crashDistances.size() - 1); // the aggregated crash distance is at the end
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFitness(IChromosome<T> chromosome, int actions) {
        return getNormalizedFitness(chromosome, actions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getNormalizedFitness(IChromosome<T> chromosome, int actions) {
        return getNormalizedFitnessVector(chromosome).get(actions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Float> getNormalizedFitnessVector(IChromosome<T> chromosome) {

        if (!actionCache.containsKey(chromosome)) {

            final List<Float> crashDistances = FitnessUtils.getCrashDistanceVector(chromosome);

            if (crashDistances.contains(0.0f)) {
                // We can terminate the search once we have reproduced the crash.
                ConditionalTerminationCondition.satisfiedCondition();
            }

            actionCache.put(chromosome, crashDistances);
        }

        return actionCache.get(chromosome);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndex() {
        return 0;
    }
}
