package org.mate.exploration.genetic.util.eda;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Collection;
import java.util.Map;

/**
 * Defines the interface for a probabilistic model used in an EDA-based approach.
 *
 * @param <T> The type of the chromosomes.
 */
public interface IProbabilisticModel<T> {

    /**
     * Updates the probabilistic model after a new population was drawn, see Algorithm 3.
     *
     * @param population The new population.
     */
    void update(Collection<IChromosome<T>> population);

    /**
     * Updates the probabilistic model with a new action transition.
     *
     * @param testCase The currently executed test case.
     * @param action The lastly executed action.
     * @param currentScreenState The new screen state.
     */
    void updatePosition(TestCase testCase, Action action, IScreenState currentScreenState);

    /**
     * Retrieves the action probabilities of the current state in the probabilistic model.
     *
     * @return Returns the action probabilities of the current state.
     */
    Map<Action, Double> getActionProbabilities();

    /**
     * Retrieves the current screen state according to the probabilistic model.
     *
     * @return Returns the current screen state.
     */
    IScreenState getState();

    /**
     * Updates the internal position of the probabilistic model.
     *
     * @param currentScreenState The new state of the probabilistic model.
     */
    void updatePositionImmutable(IScreenState currentScreenState);

    /**
     * Resets the cursor position to the root node of the probabilistic model.
     */
    void resetPosition();
}
