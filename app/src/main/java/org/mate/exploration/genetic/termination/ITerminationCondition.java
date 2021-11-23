package org.mate.exploration.genetic.termination;

/**
 * Provides the interface for a termination condition.
 */
public interface ITerminationCondition {

    /**
     * Determines whether the termination condition is met or not, e.g. if the search budget is
     * exhausted.
     *
     * @return Returns {@code} true if the termination condition is met, otherwise {@code false}
     *          is returned.
     */
    boolean isMet();
}
