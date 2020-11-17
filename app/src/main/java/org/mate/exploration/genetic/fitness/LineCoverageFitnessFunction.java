package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

import java.util.HashMap;
import java.util.Map;

public class LineCoverageFitnessFunction<T> implements IFitnessFunction<T> {
    public static final String FITNESS_FUNCTION_ID = "line_coverage_fitness_function";

    private final Map<IChromosome<T>, Double> cache;

    public LineCoverageFitnessFunction() {
        cache = new HashMap<>();
    }

    @Override
    public double getFitness(IChromosome<T> chromosome) {
        if (cache.containsKey(chromosome)) {
            return cache.get(chromosome);
        }
        // FIXME: statement coverage is not working right now
        double fitness = FitnessUtils.getFitness(chromosome);
        cache.put(chromosome, fitness);
        return fitness;
    }
}
