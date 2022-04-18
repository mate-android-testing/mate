package org.mate.exploration.genetic.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;

import java.util.HashMap;
import java.util.Map;

public class TargetActivityFitnessFunction<T> implements IFitnessFunction<T> {
    private final int maxDistance = Registry.getEnvironmentManager().getMaxActivityDistance();

    /**
     * Caches for each chromosome the computed branch distance.
     */
    private final Map<IChromosome<T>, Double> cache = new HashMap<>();

    @Override
    public double getFitness(IChromosome<T> chromosome) {
        double activityDistance = cache.computeIfAbsent(chromosome, Registry.getEnvironmentManager()::getActivityDistance);
        if (activityDistance == 0.0) {
            ConditionalTerminationCondition.satisfiedCondition();
        }
        return activityDistance;
    }

    @Override
    public boolean isMaximizing() {
        return false;
    }

    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome) / maxDistance;
    }
}
