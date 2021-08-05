package org.mate.exploration.genetic.termination;

public class ConditionalTerminationCondition implements ITerminationCondition {

    private static boolean condition = false;

    public static void satisfiedCondition() {
        condition = true;
    }

    @Override
    public boolean isMet() {
        return condition;
    }
}
