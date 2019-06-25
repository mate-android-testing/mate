package org.mate.exploration.genetic.termination;

public class NeverTerminationCondition implements ITerminationCondition {
    public static final String TERMINATION_CONDITION_ID = "never_termination_condition";

    @Override
    public boolean isMet() {
        return false;
    }
}
