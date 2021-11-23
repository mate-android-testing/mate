package org.mate.exploration.genetic.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides a fitness function that aims to maximise the activity coverage. This fitness function is
 * applicable when dealing with a {@link TestSuite} execution.
 */
// TODO: Merge with activity fitness function for test cases!
public class SuiteActivityFitnessFunction implements IFitnessFunction<TestSuite> {

    /**
     * Returns the number of covered activities for the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @return Returns the number of covered activities.
     */
    @Override
    public double getFitness(IChromosome<TestSuite> chromosome) {
        Set<String> activitiesCovered = new HashSet<>();
        for (TestCase testCase : chromosome.getValue().getTestCases()) {
            activitiesCovered.addAll(testCase.getVisitedActivities());
        }
        return activitiesCovered.size();
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} since this fitness functions aims to maximise the number of
     *          covered activities.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns the normalised fitness value, i.e. the number of covered activities divided by
     * the number of activities in total.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<TestSuite> chromosome) {
        List<String> activityNames = Registry.getEnvironmentManager().getActivityNames();
        return getFitness(chromosome) / activityNames.size();
    }
}
