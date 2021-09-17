package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

/**
 * A fitness function that prefers {@link TestSuite}s with a shorter action sequence.
 */
public class TestSuiteLengthFitnessFunction implements IFitnessFunction<TestSuite> {

    @Override
    public double getFitness(IChromosome<TestSuite> chromosome) {
        int lengthSum = 0;
        for (TestCase testCase : chromosome.getValue().getTestCases()) {
            lengthSum += testCase.getEventSequence().size();
        }
        return 1.0 / lengthSum;
    }

    /**
     * Although the intention of the fitness function is to minimise the test suite action length,
     * the fitness function itself is maximising. The function '1.0 / test suite action length'
     * describes a monotonically decreasing function, where a test case with a single action
     * returns the best value, i.e. 1.0.
     *
     * @return Returns whether this fitness function is maximising, i.e. a greater value is
     * better than a lower value, or minimising.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestSuite> chromosome) {
        return getFitness(chromosome);
    }
}
