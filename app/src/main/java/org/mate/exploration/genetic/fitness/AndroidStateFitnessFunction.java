package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

/**
 * Provides a fitness function that objective is to maximise the number of visited (screen) states.
 * This fitness function is applicable when dealing with a {@link TestCase} execution.
 */
public class AndroidStateFitnessFunction implements IFitnessFunction<TestCase> {

    /**
     * Returns the number of visited states.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @return Returns the number of visited states.
     */
    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return chromosome.getValue().getVisitedStates().size();
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} since this fitness functions aims to maximise the number of
     *          visited states.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns the normalised fitness value, i.e. the number of visited states divided by
     * {@link Double#MAX_VALUE}, since the total number of states is unknown.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return getFitness(chromosome) / Double.MAX_VALUE;
    }
}
