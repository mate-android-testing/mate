package org.mate.crash_reproduction.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;

import java.util.HashMap;
import java.util.Map;

public class ReachedRequiredConstructor implements IFitnessFunction<TestCase> {
    private final Map<IChromosome<TestCase>, Double> cache = new HashMap<>();

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return getNormalizedFitness(chromosome);
    }

    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return cache.computeIfAbsent(chromosome, Registry.getEnvironmentManager()::getReachedRequiredConstructors);
    }
}
