package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

public class TestCaseLengthFitnessFunction implements IFitnessFunction<TestCase> {

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        int testCaseLength = chromosome.getValue().getEventSequence().size();
        return 1.0 / testCaseLength;
    }

    /*
      The intention of this fitness function is to prefer chromosomes with a lower test case
      length. Considering the way to compute the fitness value it's a maximizing function.
      Assume you have a test suite with 4 and 8 test cases, then the respective fitness values
      are 1/4 and 1/8 according to getFitness().
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
