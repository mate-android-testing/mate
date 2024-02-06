package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.utils.ChromosomeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
     * NOTE: Recording the fitness after each single action is slow and one should consequently prefer
     * to record the individual fitness values in one pass, see
     * {@link #recordActionFitness(IChromosome, Map)}.
     *
     * @param chromosome The chromosome for which the fitness should be recorded.
     */
    @SuppressWarnings("unused")
    public void recordCurrentActionFitness(final IChromosome<TestCase> chromosome) {
        final double fitness = fitnessFunction.getNormalizedFitness(chromosome);
        actionFitnessValues.put(ChromosomeUtils.getActionEntityId(chromosome), fitness);
    }

    /**
     * Records the fitness values for the given actions of the given chromosome.
     * NOTE: One should use {@link #recordActionFitness(IChromosome, Map)} instead which sends a
     * single request to MATE-Server and consequently retrieves the individual fitness values in one
     * batch.
     *
     * @param chromosome The chromosome for which the fitness should be recorded.
     * @param tracesPerAction The traces per action.
     */
    @SuppressWarnings("unused")
    public void recordActionFitnessOld(final IChromosome<TestCase> chromosome,
                                    final Map<String, Set<String>> tracesPerAction) {
        for (final Map.Entry<String, Set<String>> entry : tracesPerAction.entrySet()) {
            // NOTE: The linked hashset guarantees traversing using the insertion order.
            final int actions = Integer.parseInt(entry.getKey().split("_")[0]);
            actionFitnessValues.put(entry.getKey(),
                    fitnessFunction.getNormalizedFitness(chromosome, actions));
        }
    }

    /**
     * Records the fitness values for the given actions of the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness should be recorded.
     * @param tracesPerAction The traces per action.
     */
    public void recordActionFitness(final IChromosome<TestCase> chromosome,
                                    final Map<String, Set<String>> tracesPerAction) {

        final List<Double> fitnessVector = fitnessFunction.getNormalizedFitnessVector(chromosome);

        for (final Map.Entry<String, Set<String>> entry : tracesPerAction.entrySet()) {
            // NOTE: The linked hashset guarantees traversing using the insertion order.
            final int actions = Integer.parseInt(entry.getKey().split("_")[0]);
            actionFitnessValues.put(entry.getKey() + "_" + fitnessFunction.getIndex(), fitnessVector.get(actions));
        }
    }

    /**
     * Retrieves the fitness value after the 'x-th' action from the given test case.
     *
     * @param testCase The given test case.
     * @param actions The 'x-th' action.
     * @return Returns the fitness value after the 'x-th' action.
     */
    public double getFitnessAfterXActions(final IChromosome<TestCase> testCase, final int actions) {
        return Objects.requireNonNull(actionFitnessValues.get(
                ChromosomeUtils.getActionEntityId(testCase, actions) + "_" + fitnessFunction.getIndex()));
    }
}
