package org.mate.exploration.genetic.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;

import java.util.HashMap;
import java.util.Map;

public class BasicBlockDistance<T> implements IFitnessFunction<T> {
    private final Map<IChromosome<T>, Double> cache = new HashMap<>();

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
        return cache.computeIfAbsent(chromosome, c -> Registry.getEnvironmentManager().getMergedBasicBlockDistance(c));
    }
}
