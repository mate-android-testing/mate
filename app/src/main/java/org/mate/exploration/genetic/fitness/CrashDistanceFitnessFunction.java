package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.FitnessUtils;

import java.util.List;

/**
 * Provides a fitness function for crash reproduction.
 *
 * @param <T> The chromosomes type.
 */
public class CrashDistanceFitnessFunction<T> implements IActionFitnessFunction<T> {

    @Override
    public double getFitness(IChromosome<T> chromosome) {
        return getNormalizedFitness(chromosome);
    }

    @Override
    public boolean isMaximizing() {
        return false;
    }

    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {

        final double crashDistance = FitnessUtils.getFitness(chromosome, FitnessFunction.CRASH_DISTANCE);

        if (crashDistance == 0.0d) { // We can terminate the search once we have reproduced the crash.
            ConditionalTerminationCondition.satisfiedCondition();
        }

        return crashDistance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFitness(IChromosome<T> chromosome, int actions) {
        return getNormalizedFitness(chromosome, actions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome, int actions) {

        final double crashDistance = FitnessUtils.getFitness(chromosome, actions,
                FitnessFunction.CRASH_DISTANCE);

        if (crashDistance == 0.0d) { // We can terminate the search once we have reproduced the crash.
            ConditionalTerminationCondition.satisfiedCondition();
        }

        return crashDistance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Double> getNormalizedFitnessVector(IChromosome<T> chromosome) {

        final List<Double> crashDistances = FitnessUtils.getCrashDistanceVector(chromosome);

        if (crashDistances.contains(0.0d)) {
            // We can terminate the search once we have reproduced the crash.
            ConditionalTerminationCondition.satisfiedCondition();
        }

        return crashDistances;
    }

    @Override
    public int getIndex() {
        return 0;
    }
}
