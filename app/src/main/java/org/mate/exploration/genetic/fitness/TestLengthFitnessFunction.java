package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

public class TestLengthFitnessFunction implements IFitnessFunction<TestSuite> {

    @Override
    public double getFitness(IChromosome<TestSuite> chromosome) {
        int lengthSum = 0;
        for (TestCase testCase : chromosome.getValue().getTestCases()) {
            lengthSum += testCase.getEventSequence().size();
        }
        return 1.0 / lengthSum;
    }

    @Override
    public boolean isMaximizing() {
        return false;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestSuite> chromosome) {
        return 0;
    }
}
