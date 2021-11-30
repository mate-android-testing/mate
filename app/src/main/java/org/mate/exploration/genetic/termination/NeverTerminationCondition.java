package org.mate.exploration.genetic.termination;

/**
 * Provides a termination condition that cannot be satisfied.
 */
public class NeverTerminationCondition implements ITerminationCondition {

    /**
     * Determines whether the termination condition was met or not.
     *
     * @return Returns {@code false} since the termination condition is never met.
     */
    @Override
    public boolean isMet() {
        return false;
    }
}
