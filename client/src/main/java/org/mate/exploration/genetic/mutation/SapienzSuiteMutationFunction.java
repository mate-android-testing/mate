package org.mate.exploration.genetic.mutation;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.commons.utils.Randomness;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.utils.FitnessUtils;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
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
                testCase.getActionSequence().clear();
                testCase.getActionSequence().addAll(mutatedTestCase.getActionSequence());
                notMutatedTestCases.remove(testCase);
            }
        }

        // we need to execute those test cases that have been mutated
        for (TestCase testCase : testCases) {
            if (!notMutatedTestCases.contains(testCase)) {

                TestCase executed = TestCase.fromDummy(testCase);
                mutatedTestSuite.getTestCases().add(executed);

                if (Properties.SURROGATE_MODEL()) {
                    // update sequences + write traces to external storage
                    SurrogateModel surrogateModel
                            = (SurrogateModel) Registry.getUiAbstractionLayer().getGuiModel();
                    surrogateModel.updateTestCase(executed);
                }

                FitnessUtils.storeTestSuiteChromosomeFitness(mutatedChromosome, executed);
                CoverageUtils.storeTestSuiteChromosomeCoverage(mutatedChromosome, executed);

                executed.finish();
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

    /**
     * Performs an (in-place) one-point crossover with the given two test cases.
     *
     * @param t1 The first test case.
     * @param t2 The second test case.
     */
    private void onePointCrossover(TestCase t1, TestCase t2) {

        TestCase copyT1 = TestCase.newDummy();
        copyT1.getActionSequence().addAll(t1.getActionSequence());
        TestCase copyT2 = TestCase.newDummy();
        copyT2.getActionSequence().addAll(t2.getActionSequence());

        int lengthT1 = t1.getActionSequence().size();
        int lengthT2 = t2.getActionSequence().size();
        int min = Math.min(lengthT1, lengthT2);
        int cutPoint = Randomness.getRnd().nextInt(min);

        t1.getActionSequence().clear();
        t2.getActionSequence().clear();

        t1.getActionSequence().addAll(copyT1.getActionSequence().subList(0, cutPoint));
        t1.getActionSequence().addAll(copyT2.getActionSequence().subList(cutPoint, lengthT2));

        t2.getActionSequence().addAll(copyT2.getActionSequence().subList(0, cutPoint));
        t2.getActionSequence().addAll(copyT1.getActionSequence().subList(cutPoint, lengthT1));
    }
}
