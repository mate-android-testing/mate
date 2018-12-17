package org.mate.exploration.genetic;

import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.HashSet;
import java.util.Set;

public class SuiteActivityFitnessFunction implements IFitnessFunction<TestSuite> {
    public static final String FITNESS_FUNCTION_ID = "suite_fitness_function";

    @Override
    public double getFitness(IChromosome<TestSuite> chromosome) {
        Set<String> activitiesCovered = new HashSet<>();
        for (TestCase testCase : chromosome.getValue().getTestCases()) {
            activitiesCovered.addAll(testCase.getVisitedActivities());
        }
        return activitiesCovered.size();
    }
}
