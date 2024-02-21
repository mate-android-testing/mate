package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

/**
 * Provides a mechanism to store and retrieve the fitness of a test case per action.
 */
public class ActionFitnessFunctionWrapper implements IFitnessFunction<TestCase> {

    /**
     * The underlying fitness function.
     */
    private final IActionFitnessFunction<TestCase> fitnessFunction;

    /**
     * Initialises the fitness function.
     *
     * @param fitnessFunction The underlying action-based fitness function.
     */
    public ActionFitnessFunctionWrapper(IActionFitnessFunction<TestCase> fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }

    /**
     * Returns the index of the underlying fitness function.
     *
     * @return Returns the index of the underlying fitness function.
     */
    public int getIndex() {
        return fitnessFunction.getIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getFitness(final IChromosome<TestCase> chromosome) {
        return getNormalizedFitness(chromosome);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMaximizing() {
        return fitnessFunction.isMaximizing();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNormalizedFitness(final IChromosome<TestCase> chromosome) {
        return getFitnessAfterXActions(chromosome, chromosome.getValue().getActionSequence().size());
    }

    /**
     * Retrieves the fitness value after the 'x-th' action from the given test case.
     *
     * @param testCase The given test case.
     * @param actions The 'x-th' action.
     * @return Returns the fitness value after the 'x-th' action.
     */
    public double getFitnessAfterXActions(final IChromosome<TestCase> testCase, final int actions) {
        return fitnessFunction.getNormalizedFitnessVector(testCase).get(actions);
    }
}
