package org.mate.exploration.genetic.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

import java.util.List;

public class ActivityFitnessFunction implements IFitnessFunction<TestCase> {

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return chromosome.getValue().getVisitedActivities().size();
    }

    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        List<String> activityNames = Registry.getUiAbstractionLayer().getActivityNames();
        return getFitness(chromosome) / activityNames.size();
    }
}
