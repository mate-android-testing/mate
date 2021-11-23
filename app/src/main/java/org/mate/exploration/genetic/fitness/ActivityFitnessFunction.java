package org.mate.exploration.genetic.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

import java.util.List;

/**
 * Provides an fitness function that objective is to maximize the number of visited activities.
 * This fitness function is applicable when dealing with a {@link TestCase} execution.
 */
public class ActivityFitnessFunction implements IFitnessFunction<TestCase> {

    /**
     * Returns the number of visited activities.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @return Returns the number of visited activities.
     */
    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return chromosome.getValue().getVisitedActivities().size();
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} since this fitness functions aims to maximise the number of
     *          visited activities.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns the normalised fitness value, i.e. the number of visited activities divided by
     * the number of activities in total.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        List<String> activityNames = Registry.getEnvironmentManager().getActivityNames();
        return getFitness(chromosome) / activityNames.size();
    }
}
