package org.mate.exploration.genetic.termination;

/**
 * Provides a termination condition that is met when a certain condition is satisfied.
 */
public class ConditionalTerminationCondition implements ITerminationCondition {

    /**
     * The current state of condition, e.g. not satisfied.
     */
    private static boolean condition = false;

    /**
     * Sets the condition to {@code true}, i.e. the condition is met.
     */
    public static void satisfiedCondition() {
        condition = true;
    }

    /**
     * Determines whether the termination condition is met.
     *
     * @return Returns {@code} true if the condition is met, otherwise {@code false} is returned.
     */
    @Override
    public boolean isMet() {
        return condition;
    }
}
