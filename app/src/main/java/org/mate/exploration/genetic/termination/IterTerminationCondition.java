package org.mate.exploration.genetic.termination;

public class IterTerminationCondition implements ITerminationCondition {
    public static final String TERMINATION_CONDITION_ID = "iter_termination_condition";

    private int iterations;
    private int maxIterations;

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
