package org.mate.exploration.genetic.termination;

/**
 * Termination condition that will indicate stop evolving after being called a given amount of times
 */
public class IterTerminationCondition implements ITerminationCondition {

    private int iterations;
    private int maxIterations;

    /**
     * @param maxIterations how many times should {@code isMet()} be called until returning true
     */
    public IterTerminationCondition(int maxIterations) {
        this.maxIterations = maxIterations;
        iterations = 0;
    }

    @Override
    public boolean isMet() {
        if (iterations == maxIterations) {
            return true;
        }

        iterations++;
        return false;
    }
}
