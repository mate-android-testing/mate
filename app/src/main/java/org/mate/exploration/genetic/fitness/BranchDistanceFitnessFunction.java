package org.mate.exploration.genetic.fitness;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.ui.EnvironmentManager;

import java.util.HashMap;
import java.util.Map;

public class BranchDistanceFitnessFunction implements IFitnessFunction<TestCase> {

    public static final String FITNESS_FUNCTION_ID = "branch_distance_fitness_function";

    private final Map<IChromosome<TestCase>, Double> cache;

    public BranchDistanceFitnessFunction() {
        cache = new HashMap<>();
    }

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        MATE.log("Retrieving Branch Distance...");
        if (cache.containsKey(chromosome)) {
            return cache.get(chromosome);
        }
        double fitness = EnvironmentManager.getBranchDistance(chromosome);
        MATE.log("Branch Distance: " + fitness);
        cache.put(chromosome, fitness);
        return fitness;
    }
}
