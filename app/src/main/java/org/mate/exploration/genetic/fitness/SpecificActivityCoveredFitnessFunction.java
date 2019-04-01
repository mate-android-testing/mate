package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

public class SpecificActivityCoveredFitnessFunction implements IFitnessFunction<TestCase> {
    public static final String FITNESS_FUNCTION_ID = "specific_activity_covered_fitness_function";
    private final String activityName;

    public SpecificActivityCoveredFitnessFunction(String activityName) {
        this.activityName = activityName;
    }

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        if (chromosome.getValue().getVisitedActivities().contains(activityName)) {
            return 1;
        } else {
            return 0;
        }
    }
}
