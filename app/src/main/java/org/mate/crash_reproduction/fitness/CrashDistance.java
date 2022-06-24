package org.mate.crash_reproduction.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.CallTreeDistance;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoverageFitnessFunction;
import org.mate.model.TestCase;

import java.util.HashMap;
import java.util.Map;

public class CrashDistance implements IFitnessFunction<TestCase> {
    private final Map<IFitnessFunction<TestCase>, Double> weightedFitnessFunctions = new HashMap<IFitnessFunction<TestCase>, Double>(){{
//        put(new BankdroidFitnessFunction(), 1D);

        put(new CallTreeDistance<>(), 6D);
        put(new ReachedTarget(), 3D);
        put(new EndsOnTarget(), 2D);
        put(new LineCoverageFitnessFunction<>(), 1D);
    }};

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
}
