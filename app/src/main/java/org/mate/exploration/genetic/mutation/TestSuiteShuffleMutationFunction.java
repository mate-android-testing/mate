package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestSuite;
import org.mate.utils.Randomness;

/**
 * Provides a test suite shuffle mutation.
 */
public class TestSuiteShuffleMutationFunction implements IMutationFunction<TestSuite> {

    /**
     * Shuffles the test cases in the given test suite chromosome. Note that the operation happens
     * in place to avoid copying fitness/coverage data.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the shuffled test suite.
     */
    @Override
    public IChromosome<TestSuite> mutate(IChromosome<TestSuite> chromosome) {
        TestSuite testSuite = chromosome.getValue();
        Randomness.shuffleList(testSuite.getTestCases());
        return chromosome;
    }
}
