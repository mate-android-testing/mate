package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

/**
 * A fitness function that prefers {@link TestCase}s with a shorter action sequence.
 */
public class TestCaseLengthFitnessFunction implements IFitnessFunction<TestCase> {

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        int testCaseLength = chromosome.getValue().getEventSequence().size();
        return 1.0 / testCaseLength;
    }

    /**
     * Although the intention of the fitness function is to minimise the test case length,
     * the fitness function itself is maximising. The function '1.0 / test case length' describes
     * a monotonically decreasing function, where a test case with a single action returns
     * the best value, i.e. 1.0.
     *
     * @return Returns whether this fitness function is maximising, i.e. a greater value is
     * better than a lower value, or minimising.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return getFitness(chromosome);
    }
}
