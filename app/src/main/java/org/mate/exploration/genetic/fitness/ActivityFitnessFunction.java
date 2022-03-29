package org.mate.exploration.genetic.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an fitness function that objective is to maximize the number of visited activities.
 */
public class ActivityFitnessFunction<T> implements IFitnessFunction<T> {

    /**
     * Returns the number of visited activities.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @return Returns the number of visited activities.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {
        if (chromosome.getValue() instanceof TestCase) {
            return ((TestCase) chromosome.getValue()).getVisitedActivitiesOfApp().size();
        } else if (chromosome.getValue() instanceof TestSuite) {
            Set<String> coveredActivities = new HashSet<>();
            for (TestCase testCase : ((TestSuite) chromosome.getValue()).getTestCases()) {
                coveredActivities.addAll(testCase.getVisitedActivitiesOfApp());
            }
            return coveredActivities.size();
        } else {
            throw new UnsupportedOperationException("Chromosome type "
                    + chromosome.getValue().getClass() + " not yet supported!");
        }
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
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        List<String> activities = Registry.getUiAbstractionLayer().getActivities();
        return getFitness(chromosome) / activities.size();
    }
}
