package org.mate.exploration.genetic.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SuiteActivityFitnessFunction implements IFitnessFunction<TestSuite> {

    @Override
    public double getFitness(IChromosome<TestSuite> chromosome) {
        Set<String> activitiesCovered = new HashSet<>();
        for (TestCase testCase : chromosome.getValue().getTestCases()) {
            activitiesCovered.addAll(testCase.getVisitedActivities());
        }
        return activitiesCovered.size();
    }

    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestSuite> chromosome) {
        List<String> activityNames = Registry.getActivityNames();
        return getFitness(chromosome) / activityNames.size();
    }
}
