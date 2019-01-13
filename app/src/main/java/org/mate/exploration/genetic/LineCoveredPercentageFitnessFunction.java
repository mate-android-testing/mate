package org.mate.exploration.genetic;

import org.mate.model.TestCase;
import org.mate.ui.EnvironmentManager;

import java.util.HashMap;
import java.util.Map;

public class LineCoveredPercentageFitnessFunction implements IFitnessFunction<TestCase> {
    public static final String FITNESS_FUNCTION_ID = "line_covered_percentage_fitness_function";

    private final Map<IChromosome<TestCase>, Double> cache;
    private final String line;

    public LineCoveredPercentageFitnessFunction(String line) {
        this.line = line;
        cache = new HashMap<>();
    }

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        if (cache.containsKey(chromosome)) {
            return cache.get(chromosome);
        }
        double fitness = EnvironmentManager.getLineCoveredPercentage(chromosome, line);
        cache.put(chromosome, fitness);
        return fitness;
    }
}
