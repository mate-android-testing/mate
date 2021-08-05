package org.mate.exploration.genetic.termination;

/**
 * Termination condition that will never be met
 */
public class NeverTerminationCondition implements ITerminationCondition {

    @Override
    public boolean isMet() {
        return false;
    }
}
