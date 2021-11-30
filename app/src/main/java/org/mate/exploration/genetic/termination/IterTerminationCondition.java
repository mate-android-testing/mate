package org.mate.exploration.genetic.termination;

/**
 * Provides a termination condition that satisfied when a certain amount of iterations happened.
 */
public class IterTerminationCondition implements ITerminationCondition {

    /**
     * The current number of performed iterations.
     */
    private int iterations;

    /**
     * The maximal number of iterations.
     */
    private final int maxIterations;

    /**
     * Initialises the termination condition with the maximal number of iterations.
     *
     * @param maxIterations The maximal number of iterations.
     */
    public IterTerminationCondition(int maxIterations) {
        this.maxIterations = maxIterations;
        iterations = 0;
    }

    /**
     * Determines whether the maximal number of iterations is reached or not.
     *
     * @return Returns {@code true} if the maximal number of iterations is reached, otherwise
     *          {@code false} is returned.
     */
    @Override
    public boolean isMet() {
        if (iterations == maxIterations) {
            return true;
        } else {
            iterations++;
            return false;
        }
    }
}
