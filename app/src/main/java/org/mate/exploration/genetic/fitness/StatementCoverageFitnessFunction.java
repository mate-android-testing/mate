package org.mate.exploration.genetic.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.ui.EnvironmentManager;

import java.util.HashMap;
import java.util.Map;

public class StatementCoverageFitnessFunction<T> implements IFitnessFunction<T> {
    public static final String FITNESS_FUNCTION_ID = "statement_coverage_fitness_function";

    private final Map<IChromosome<T>, Double> cache;

    public StatementCoverageFitnessFunction() {
        cache = new HashMap<>();
    }

    @Override
    public double getFitness(IChromosome<T> chromosome) {
        if (cache.containsKey(chromosome)) {
            return cache.get(chromosome);
        }
        double fitness = Registry.getEnvironmentManager().getCoverage(chromosome);
        cache.put(chromosome, fitness);
        return fitness;
    }
}
