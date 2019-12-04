package org.mate.exploration.genetic.mutation;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.crossover.TestCaseMergeCrossOverFunction;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.ui.EnvironmentManager;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SapienzSuiteMutationFunction implements IMutationFunction<TestSuite> {
    public static final String MUTATION_FUNCTION_ID = "sapienz_suite_mutation_function";

    private final double pMutate;
    private final TestCaseMergeCrossOverFunction testCaseMergeCrossOverFunction;
    private final TestCaseShuffleMutationFunction testCaseShuffleMutationFunction;
    private final boolean storeCoverage;

    public SapienzSuiteMutationFunction(double pMutate) {
        this(Properties.STORE_COVERAGE(), pMutate);
    }

    public SapienzSuiteMutationFunction(boolean storeCoverage, double pMutate) {
        this.pMutate = pMutate;
        this.storeCoverage = storeCoverage;
        testCaseMergeCrossOverFunction = new TestCaseMergeCrossOverFunction(false);
        testCaseMergeCrossOverFunction.setExecuteActions(false);
        testCaseShuffleMutationFunction = new TestCaseShuffleMutationFunction(false);
        testCaseShuffleMutationFunction.setExecuteActions(false);
    }

    @Override
    public List<IChromosome<TestSuite>> mutate(IChromosome<TestSuite> chromosome) {
        TestSuite mutatedTestSuite = new TestSuite();
        List<IChromosome<TestSuite>> mutations = new ArrayList<>();
        IChromosome<TestSuite> mutatedChromosome = new Chromosome<>(mutatedTestSuite);
        mutations.add(mutatedChromosome);

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

        /* don't do this for now
        for (TestCase testCase : afterOnePointCrossover) {
            if (Randomness.getRnd().nextDouble() < pMutate) {
                TestCase mutatedTc = testCaseShuffleMutationFunction.mutate(
                        new Chromosome<TestCase>(testCase)).get(0).getValue();
                afterInternalMutation.add(mutatedTc);
            } else {
                afterInternalMutation.add(testCase);
            }
        }
        */
        //remove this line, if lines above get uncommented
        afterInternalMutation.addAll(afterOnePointCrossover);

        List<TestCase> copyTestCases = new ArrayList<>();
        for (TestCase testCase : afterInternalMutation) {
            if (testCase.getId().equals("dummy")) {
                TestCase mutatedTestCase = TestCase.fromDummy(testCase);
                executedTestCases.add(mutatedTestCase);
                if (storeCoverage) {
                    Registry.getEnvironmentManager().storeCoverageData(mutatedChromosome, mutatedTestCase);

                    MATE.log_acc("Coverage of: " + chromosome.toString() + ": " + Registry.getEnvironmentManager()
                            .getCoverage(chromosome));
                    MATE.log_acc("Found crash: " + String.valueOf(mutatedTestCase.getCrashDetected()));
                }
            } else {
                executedTestCases.add(testCase);
                if (storeCoverage) {
                    copyTestCases.add(testCase);
                }
            }
        }

        if (!copyTestCases.isEmpty()) {
            Registry.getEnvironmentManager().copyCoverageData(chromosome, mutatedChromosome, copyTestCases);
        }

        mutatedTestSuite.getTestCases().addAll(executedTestCases);

        return mutations;
    }
}
