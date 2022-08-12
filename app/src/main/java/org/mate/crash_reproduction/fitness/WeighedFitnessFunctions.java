package org.mate.crash_reproduction.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;

import java.util.Map;
import java.util.Set;

public class WeighedFitnessFunctions implements IMultipleFitnessFunctions<TestCase> {
    private final Map<IFitnessFunction<TestCase>, Double> weightedFitnessFunctions;

    public WeighedFitnessFunctions(Map<IFitnessFunction<TestCase>, Double> weightedFitnessFunctions) {
        this.weightedFitnessFunctions = weightedFitnessFunctions;
    }

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return getNormalizedFitness(chromosome);
    }

    @Override
    public boolean isMaximizing() {
        return false;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        double weightSum = 0;
        double weightedFitness = 0;

        for (Map.Entry<IFitnessFunction<TestCase>, Double> weightedFitnessFunction : weightedFitnessFunctions.entrySet()) {
            double fitness = weightedFitnessFunction.getKey().getNormalizedFitness(chromosome);
            if (weightedFitnessFunction.getKey().isMaximizing()) {
                // Maximising to minimizing fitness
                fitness = 1 - fitness;
            }

            weightSum += weightedFitnessFunction.getValue();
            weightedFitness += weightedFitnessFunction.getValue() * fitness;
        }

        return weightedFitness / weightSum;
    }

    @Override
    public Set<IFitnessFunction<TestCase>> getInnerFitnessFunction() {
        return weightedFitnessFunctions.keySet();
    }

    public Map<IFitnessFunction<TestCase>, Double> getWeightedFitnessFunctions() {
        return weightedFitnessFunctions;
    }
}
