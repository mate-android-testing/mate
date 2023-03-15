package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.utils.FitnessUtils;

import java.util.List;

public class CrashDistance<T> implements IFitnessFunction<T> {

    // TODO: May remove if not needed here.
    private final List<String> targetStackTrace;

    public CrashDistance(List<String> targetStackTrace) {
        this.targetStackTrace = targetStackTrace;
    }

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
