package org.mate.utils;

import org.mate.MATE;
import org.mate.exploration.genetic.fitness.ActionFitnessFunctionWrapper;

public class FitnessFunctionState {

    /**
     * The fitness function to evaluate the target, e.g., branch, associated with the probabilistic model.
     */
    private final ActionFitnessFunctionWrapper fitnessFunction;

    /**
     * The currently best fitness value.
     */
    private double bestFitness;

    /**
     * Constructs a new fitness function state.
     *
     * @param fitnessFunction The action-based fitness function.
     */
    public FitnessFunctionState (ActionFitnessFunctionWrapper fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
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
     * @return Returns {@code true} when the given fitness was better than the previous,
     *          otherwise {@code false}.
     */
    public boolean updateFitness(final double fitness) {
        if (fitnessFunction.isMaximizing()) {
            if (fitness > this.bestFitness) {
                MATE.log_debug("Fitness increase from " + this.bestFitness + " to " + fitness);
                this.bestFitness = fitness;
                return true;
            }
        } else {
            if (fitness < this.bestFitness) {
                MATE.log_debug("Fitness decreased from " + this.bestFitness + " to " + fitness);
                this.bestFitness = fitness;
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the probabilistic model has been covered according to the associated fitness
     * function.
     *
     * @return Returns {@code true} if the target has been covered, otherwise {@code false}.
     */
    public boolean isCovered() {
        return fitnessFunction.isMaximizing() ? bestFitness == 1.0d : bestFitness == 0.0d;
    }

    /**
     * Returns the action-based fitness function.
     *
     * @return Returns the action-based fitness function.
     */
    public ActionFitnessFunctionWrapper getFitnessFunction() {
        return fitnessFunction;
    }
}
