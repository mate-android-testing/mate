package org.mate.exploration.genetic.termination;

public class ConditionalTerminationCondition implements ITerminationCondition {

    public static final String TERMINATION_CONDITION_ID = "conditional_termination_condition";

    private static boolean condition = false;

    public static void satisfiedCondition() {
        condition = true;
    }

    @Override
    public boolean isMet() {
        return condition;
    }
}
