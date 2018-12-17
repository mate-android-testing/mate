package org.mate.exploration.genetic;

import org.mate.model.TestCase;
import org.mate.model.TestSuite;

public class TestLengthFitnessFunction implements IFitnessFunction<TestSuite> {
    public static final String FITNESS_FUNCTION_ID = "test_length_fitness_function";

    @Override
    public double getFitness(IChromosome<TestSuite> chromosome) {
        int lengthSum = 0;
        for (TestCase testCase : chromosome.getValue().getTestCases()) {
            lengthSum += testCase.getEventSequence().size();
        }
        return 1.0 / lengthSum;
    }
}
