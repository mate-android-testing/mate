package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.FitnessUtils;

/**
 * Provides a fitness function for crash reproduction.
 *
 * @param <T> The chromosomes type.
 */
public class CrashDistanceFitnessFunction<T> implements IFitnessFunction<T> {

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

        if (crashDistance == 0.0d) {
            ConditionalTerminationCondition.satisfiedCondition();
        }

        return crashDistance;
    }
}
