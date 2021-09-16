package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

public class TestSuiteLengthFitnessFunction implements IFitnessFunction<TestSuite> {

    @Override
    public double getFitness(IChromosome<TestSuite> chromosome) {
        int lengthSum = 0;
        for (TestCase testCase : chromosome.getValue().getTestCases()) {
            lengthSum += testCase.getEventSequence().size();
        }
        return 1.0 / lengthSum;
    }

    /*
      The intention of this fitness function is to prefer chromosomes with a lower test case
      length. Considering the way to compute the fitness value it's a maximizing function.
      Assume you have a test suite with 4 and 8 test cases, then the respective fitness values
      are 1/4 and 1/8 according to getFitness(). Obviously, 1/4 is greater than 1/8, thus better.
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
