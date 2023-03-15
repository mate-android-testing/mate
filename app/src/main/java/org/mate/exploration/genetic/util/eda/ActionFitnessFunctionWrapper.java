package org.mate.exploration.genetic.util.eda;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;
import org.mate.utils.ChromosomeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provides a mechanism to store and retrieve the fitness of a test case per action.
 */
public class ActionFitnessFunctionWrapper implements IFitnessFunction<TestCase> {

    /**
     * Stores for every action the associated fitness value.
     */
    private static final Map<String, Double> actionFitnessValues = new HashMap<>();

    /**
     * The underlying fitness function.
     */
    private final IFitnessFunction<TestCase> fitnessFunction;

    /**
     * Initialises the fitness function.
     *
     * @param fitnessFunction The underlying fitness function.
     */
    public ActionFitnessFunctionWrapper(IFitnessFunction<TestCase> fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
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
     * Records the fitness value after the execution of the last action.
     *
     * @param chromosome The chromosome for which the fitness should be recorded.
     */
    public void recordCurrentActionFitness(final IChromosome<TestCase> chromosome) {

        MATE.log("Recording action fitness for chromosome: " + chromosome
                + "(" + ChromosomeUtils.getActionEntityId(chromosome) + ")");

        final double fitness = fitnessFunction.getNormalizedFitness(chromosome);
        actionFitnessValues.put(ChromosomeUtils.getActionEntityId(chromosome), fitness);
        MATE.log("Testcase fitness after " + chromosome.getValue().getActionSequence().size() + " actions is: " + fitness);
    }

    /**
     * Retrieves the fitness value after the 'x-th' action from the given test case.
     *
     * @param testCase The given test case.
     * @param actions The 'x-th' action.
     * @return Returns the fitness value after the 'x-th' action.
     */
    public double getFitnessAfterXActions(final IChromosome<TestCase> testCase, final int actions) {
        MATE.log("Retrieving action fitness for chromosome: "+ testCase
                + "(" + actions + ")");
        return Objects.requireNonNull(actionFitnessValues.get(
                ChromosomeUtils.getActionEntityId(testCase, actions)));
    }
}
