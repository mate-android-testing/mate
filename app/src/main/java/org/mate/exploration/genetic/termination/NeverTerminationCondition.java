package org.mate.exploration.genetic.termination;

/**
 * Termination condition that will never be met
 */
public class NeverTerminationCondition implements ITerminationCondition {
    public static final String TERMINATION_CONDITION_ID = "never_termination_condition";

    @Override
    public boolean isMet() {
        return false;
    }
}
