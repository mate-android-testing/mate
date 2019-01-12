package org.mate.exploration.genetic;

import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SapienzSuiteMutationFunction implements IMutationFunction<TestSuite> {
    private final double pMutate;
    private final TestCaseMergeCrossOverFunction testCaseMergeCrossOverFunction;
    private final TestCaseShuffleMutationFunction testCaseShuffleMutationFunction;

    public SapienzSuiteMutationFunction(double pMutate) {
        this.pMutate = pMutate;
        testCaseMergeCrossOverFunction = new TestCaseMergeCrossOverFunction(false);
        testCaseMergeCrossOverFunction.setExecuteActions(false);
        testCaseShuffleMutationFunction = new TestCaseShuffleMutationFunction(false);
        testCaseShuffleMutationFunction.setExecuteActions(false);
    }

    @Override
    public List<IChromosome<TestSuite>> mutate(IChromosome<TestSuite> chromosome) {
        TestSuite mutatedTestSuite = new TestSuite();
        List<IChromosome<TestSuite>> mutations = new ArrayList<>();
        mutations.add(new Chromosome<>(mutatedTestSuite));

        List<TestCase> oldTestCases = chromosome.getValue().getTestCases();
        Randomness.shuffleList(oldTestCases);

        List<TestCase> afterOnePointCrossover = new ArrayList<>();
        List<TestCase> afterInternalMutation = new ArrayList<>();
        List<TestCase> executedTestCases = new ArrayList<>();

        if (oldTestCases.size() > 1) {
            for (int i = 1; i < oldTestCases.size(); i += 2) {
                if (Randomness.getRnd().nextDouble() < pMutate) {
                    IChromosome<TestCase> old1 = new Chromosome<>(oldTestCases.get(i - 1));
                    IChromosome<TestCase> old2 = new Chromosome<>(oldTestCases.get(i));
                    TestCase new1 = testCaseMergeCrossOverFunction.cross(Arrays.asList(old1, old2)).getValue();
                    TestCase new2 = testCaseMergeCrossOverFunction.cross(Arrays.asList(old2, old1)).getValue();
                    afterOnePointCrossover.add(new1);
                    afterOnePointCrossover.add(new2);
                } else {
                    afterOnePointCrossover.add(oldTestCases.get(i - 1));
                    afterOnePointCrossover.add(oldTestCases.get(i));
                }
            }
        }
        //add old testcase without crossover partner
        if (oldTestCases.size() % 2 != 0) {
            afterOnePointCrossover.add(oldTestCases.get(oldTestCases.size() - 1));
        }

        for (TestCase testCase : afterOnePointCrossover) {
            if (Randomness.getRnd().nextDouble() < pMutate) {
                TestCase mutatedTc = testCaseShuffleMutationFunction.mutate(
                        new Chromosome<TestCase>(testCase)).get(0).getValue();
                afterInternalMutation.add(mutatedTc);
            } else {
                afterInternalMutation.add(testCase);
            }
        }

        for (TestCase testCase : afterInternalMutation) {
            executedTestCases.add(TestCase.fromDummy(testCase));
        }

        mutatedTestSuite.getTestCases().addAll(executedTestCases);

        return mutations;
    }
}
