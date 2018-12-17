package org.mate.exploration.genetic;

import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.Randomness;

import java.util.List;

public class SuiteCutPointMutationFunction implements IMutationFunction<TestSuite> {
    private final CutPointMutationFunction cutPointMutationFunction;

    public SuiteCutPointMutationFunction(int maxNumEvents) {
        cutPointMutationFunction = new CutPointMutationFunction(maxNumEvents);
    }

    @Override
    public List<IChromosome<TestSuite>> mutate(IChromosome<TestSuite> chromosome) {
        int randomElementIndex = Randomness.getRnd().nextInt(
                chromosome.getValue().getTestCases().size());
        TestSuite mutatedTestSuite = new TestSuite();
        for (int i = 0; i < chromosome.getValue().getTestCases().size(); i++) {
            if (i == randomElementIndex) {
                TestCase mutatedTestCase = cutPointMutationFunction.mutate(new Chromosome<>(
                        chromosome.getValue().getTestCases().get(i))).get(0).getValue();
            }
        }
        for (TestCase testCase : chromosome.getValue().getTestCases()) {
            if ()
        }
    }
}
