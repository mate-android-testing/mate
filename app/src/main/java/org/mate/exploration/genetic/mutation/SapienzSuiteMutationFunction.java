package org.mate.exploration.genetic.mutation;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.crossover.TestCaseMergeCrossOverFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a mutation operator as defined in the Sapienz paper, see
 * https://discovery.ucl.ac.uk/id/eprint/1508043/1/p_issta16_sapienz.pdf. In particular, have
 * a look at section 3.1 and Algorithm 2.
 */
public class SapienzSuiteMutationFunction implements IMutationFunction<TestSuite> {

    /**
     * The probability for mutation.
     */
    private final double pMutate;

    /**
     * The inner-individual crossover operator as described in section 3.1.
     */
    private final TestCaseMergeCrossOverFunction testCaseMergeCrossOverFunction;

    /**
     * The inner-individual mutation operator as described in section 3.1.
     */
    private final TestCaseShuffleMutationFunction testCaseShuffleMutationFunction;

    /**
     * Initialises the mutation function.
     *
     * @param pMutate The probability for mutation.
     */
    public SapienzSuiteMutationFunction(double pMutate) {
        this.pMutate = pMutate;
        testCaseMergeCrossOverFunction = new TestCaseMergeCrossOverFunction();
        testCaseMergeCrossOverFunction.setExecuteActions(false);
        testCaseShuffleMutationFunction = new TestCaseShuffleMutationFunction(false);
        testCaseShuffleMutationFunction.setExecuteActions(false);
    }

    /**
     * Performs variation as described in the Sapienz paper, see section 3.1 and Algorithm 2.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<TestSuite> mutate(IChromosome<TestSuite> chromosome) {

        TestSuite mutatedTestSuite = new TestSuite();
        IChromosome<TestSuite> mutatedChromosome = new Chromosome<>(mutatedTestSuite);

        List<TestCase> testCases = chromosome.getValue().getTestCases();
        List<TestCase> notMutatedTestCases = new ArrayList<>(testCases);

        // shuffle the test cases within the test suite
        Randomness.shuffleList(testCases);

        for (int i = 1; i < testCases.size(); i = i + 2) {
            double rnd = Randomness.getRnd().nextDouble();

            if (rnd < pMutate) { // if r < q
                /*
                * Sapienz performs a one-point crossover on two neighbouring test cases. Since
                * MATE only supports crossover functions that return a single offspring, we make
                * the one-point crossover here in place.
                 */
                TestCase t1 = testCases.get(i - 1);
                TestCase t2 = testCases.get(i);
                onePointCrossover(t1, t2);
                notMutatedTestCases.remove(t1);
                notMutatedTestCases.remove(t2);
            }
        }

        for (int i = 0; i < testCases.size(); i++) {
            double rnd = Randomness.getRnd().nextDouble();

            TestCase testCase = testCases.get(i);

            if (rnd < pMutate) { // if r < q

                IChromosome<TestCase> mutant
                        = testCaseShuffleMutationFunction.mutate(new Chromosome<>(testCase));
                TestCase mutatedTestCase = mutant.getValue();

                // we need to make the mutation in place
                testCase.getEventSequence().clear();
                testCase.getEventSequence().addAll(mutatedTestCase.getEventSequence());
                notMutatedTestCases.remove(testCase);
            }
        }

        MATE.log_acc("Number of test cases: " + testCases.size());
        MATE.log_acc("Number of not mutated test cases: " + notMutatedTestCases.size());
        int mutatedTestCases = testCases.size() - notMutatedTestCases.size();
        MATE.log_acc("Number of mutated test cases: " + mutatedTestCases);

        // we need to execute those test cases that have been mutated
        for (TestCase testCase : testCases) {
            if (!notMutatedTestCases.contains(testCase)) {

                TestCase executed = TestCase.fromDummy(testCase);
                mutatedTestSuite.getTestCases().add(executed);

                FitnessUtils.storeTestSuiteChromosomeFitness(mutatedChromosome, executed);
                CoverageUtils.storeTestSuiteChromosomeCoverage(mutatedChromosome, executed);
            } else {
                mutatedTestSuite.getTestCases().add(testCase);
            }
        }

        // we need to copy fitness and coverage data for those test cases that haven't be mutated
        if (!notMutatedTestCases.isEmpty()) {
            if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
                CoverageUtils.copyCoverageData(chromosome, mutatedChromosome, notMutatedTestCases);
            }
            FitnessUtils.copyFitnessData(chromosome, mutatedChromosome, notMutatedTestCases);
        }

        CoverageUtils.logChromosomeCoverage(mutatedChromosome);
        return mutatedChromosome;
    }

    private void onePointCrossover(TestCase t1, TestCase t2) {

        TestCase copyT1 = TestCase.newDummy();
        copyT1.getEventSequence().addAll(t1.getEventSequence());
        TestCase copyT2 = TestCase.newDummy();
        copyT2.getEventSequence().addAll(t2.getEventSequence());

        int lengthT1 = t1.getEventSequence().size();
        int lengthT2 = t2.getEventSequence().size();
        int min = Math.min(lengthT1, lengthT2);
        int cutPoint = Randomness.getRnd().nextInt(min);

        t1.getEventSequence().clear();
        t2.getEventSequence().clear();

        t1.getEventSequence().addAll(copyT1.getEventSequence().subList(0, cutPoint));
        t1.getEventSequence().addAll(copyT2.getEventSequence().subList(cutPoint, lengthT2));

        t2.getEventSequence().addAll(copyT2.getEventSequence().subList(0, cutPoint));
        t2.getEventSequence().addAll(copyT1.getEventSequence().subList(cutPoint, lengthT1));
    }

    /**
     * Performs variation as described in the Sapienz paper, see section 3.1 and Algorithm 2.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    // @Override
    public IChromosome<TestSuite> mutate2(IChromosome<TestSuite> chromosome) {

        TestSuite mutatedTestSuite = new TestSuite();
        IChromosome<TestSuite> mutatedChromosome = new Chromosome<>(mutatedTestSuite);

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

        // add old testcase without crossover partner
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

        // remove this line, if lines above get uncommented
        afterInternalMutation.addAll(afterOnePointCrossover);

        List<TestCase> copyTestCases = new ArrayList<>();
        for (TestCase testCase : afterInternalMutation) {
            if (testCase.isDummy()) {
                TestCase mutatedTestCase = TestCase.fromDummy(testCase);
                executedTestCases.add(mutatedTestCase);

                FitnessUtils.storeTestSuiteChromosomeFitness(mutatedChromosome, mutatedTestCase);
                CoverageUtils.storeTestSuiteChromosomeCoverage(mutatedChromosome, mutatedTestCase);
                CoverageUtils.logChromosomeCoverage(mutatedChromosome);

            } else {
                executedTestCases.add(testCase);
                if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
                    copyTestCases.add(testCase);
                }
            }
        }

        if (!copyTestCases.isEmpty()) {
            CoverageUtils.copyCoverageData(chromosome, mutatedChromosome, copyTestCases);
            FitnessUtils.copyFitnessData(chromosome, mutatedChromosome, copyTestCases);
        }

        mutatedTestSuite.getTestCases().addAll(executedTestCases);
        return mutatedChromosome;
    }
}
