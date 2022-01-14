package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

/**
 * Provides a fitness function that aims to cover a specific activity. This fitness function is
 * applicable when dealing with a {@link TestCase} execution.
 */
public class SpecificActivityCoveredFitnessFunction implements IFitnessFunction<TestCase> {

    /**
     * The activity that should be covered.
     */
    private final String activityName;

    /**
     * Initialises the fitness function with the given activity as target.
     *
     * @param activityName The activity that should be covered.
     */
    public SpecificActivityCoveredFitnessFunction(String activityName) {
        this.activityName = activityName;
    }

    /**
     * Returns the activity coverage for the given chromosome, i.e. a value of 1 is returned if the
     * targeted activity is covered, otherwise a value of 0 is returned.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @return Returns a value of 0 if the activity was not covered, otherwise 1 is returned.
     */
    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        if (chromosome.getValue().getVisitedActivities().contains(activityName)) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} since this fitness functions aims to cover a specific activity.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns the normalised fitness value for the given chromosome.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return getFitness(chromosome);
    }
}
