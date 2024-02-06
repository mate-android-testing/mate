package org.mate.exploration.genetic.util.eda;

import org.mate.MATE;
import org.mate.exploration.genetic.fitness.ActionFitnessFunctionWrapper;

/**
 * Associates a {@link IProbabilisticModel} with an {@link ActionFitnessFunctionWrapper} to track
 * which probabilistic model has been covered according to the fitness function.
 *
 * @param <T> The type wrapped by the chromosomes.
 */
public class ProbabilisticModelState<T> {

    /**
     * The probabilistic model.
     */
    private final IProbabilisticModel<T> probabilisticModel;

    /**
     * Whether the associated target has been covered according to the fitness function.
     */
    private boolean covered = false;

    /**
     * The currently best fitness value.
     */
    private double bestFitness;

    /**
     * The fitness function to evaluate the target, e.g., branch, associated with the probabilistic model.
     */
    private final ActionFitnessFunctionWrapper fitnessFunction;

    /**
     * Constructs a new probabilistic model state.
     *
     * @param fitnessFunction The fitness function associated with the probabilistic model.
     * @param probabilisticModel The probabilistic model.
     */
    public ProbabilisticModelState(ActionFitnessFunctionWrapper fitnessFunction,
                                   IProbabilisticModel<T> probabilisticModel) {
        this.fitnessFunction = fitnessFunction;
        this.probabilisticModel = probabilisticModel;
        this.bestFitness = fitnessFunction.isMaximizing() ? 0 : 1;
    }

    /**
     * Retrieves the best fitness value observed so far.
     *
     * @return Returns the best fitness value.
     */
    public double getBestFitness() {
        return bestFitness;
    }

    /**
     * Updates the fitness if better than the current fitness value and inherently checks whether
     * the associated target has been covered.
     *
     * @param fitness The new fitness value.
     */
    public void updateFitness(final double fitness) {
        if (fitnessFunction.isMaximizing()) {
            if (fitness > this.bestFitness) {
                MATE.log_acc("Fitness increase from " + this.bestFitness + " to " + fitness);
                if (fitness >= 1d) covered = true;
                this.bestFitness = fitness;
            }
        } else {
            if (fitness < this.bestFitness) {
                MATE.log_acc("Fitness decreased from " + this.bestFitness + " to " + fitness);
                if (fitness <= 0d) covered = true;
                this.bestFitness = fitness;
            }
        }
    }

    /**
     * Checks whether the probabilistic model has been covered according to the associated fitness
     * function.
     *
     * @return Returns {@code true} if the target has been covered, otherwise {@code false}.
     */
    public boolean isCovered() {
        return covered;
    }

    /**
     * Returns the probabilistic model.
     *
     * @return Returns the probabilistic model.
     */
    public IProbabilisticModel<T> getProbabilisticModel() {
        return probabilisticModel;
    }

    /**
     * Returns the fitness function.
     *
     * @return Returns the fitness function.
     */
    public ActionFitnessFunctionWrapper getFitnessFunction() {
        return fitnessFunction;
    }
}
