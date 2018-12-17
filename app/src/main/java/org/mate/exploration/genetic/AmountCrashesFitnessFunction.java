package org.mate.exploration.genetic;

import org.mate.model.TestCase;
import org.mate.model.TestSuite;

public class AmountCrashesFitnessFunction implements IFitnessFunction<TestSuite> {
    public static final String FITNESS_FUNCTION_ID = "amount_crashes_fitness_function";

    @Override
    public double getFitness(IChromosome<TestSuite> chromosome) {
        int amountCrashes = 0;
        for (TestCase testCase : chromosome.getValue().getTestCases()) {
            amountCrashes += testCase.getCrashDetected() ? 1 : 0;
        }
        return amountCrashes;
    }
}
