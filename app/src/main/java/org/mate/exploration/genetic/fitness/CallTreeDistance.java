package org.mate.exploration.genetic.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;

import java.util.HashMap;
import java.util.Map;

public class CallTreeDistance<T> implements IFitnessFunction<T> {
    private final Map<IChromosome<T>, Double> cache = new HashMap<>();

    @Override
    public double getFitness(IChromosome<T> chromosome) {
        return cache.computeIfAbsent(chromosome, Registry.getEnvironmentManager()::getCallTreeDistance);
    }

    @Override
    public boolean isMaximizing() {
        return false;
    }

    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome);
    }
}
