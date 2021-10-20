package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

public class SpecificActivityCoveredFitnessFunction implements IFitnessFunction<TestCase> {

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

    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return getFitness(chromosome);
    }
}
