package org.mate.exploration.genetic;

/**
 * Interface that determines if the termination condition is met
 */
public interface ITerminationCondition {
    /**
     * Check if termination condition is met
     * @return whether termination condition is met
     */
    boolean isMet();
}
