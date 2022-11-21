package org.mate.exploration.genetic.mutation;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.crossover.OnePointCrossOverFunction;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a mutation operator as defined in the Sapienz paper, see
 * https://discovery.ucl.ac.uk/id/eprint/1508043/1/p_issta16_sapienz.pdf. In particular, have
 * a look at section 3.1 and Algorithm 2.
 */
public class SapienzTest
        implements IMutationWithCrossOver<TestCase, TestSuite> {

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
    public SapienzTest(double pMutate) {
        this.pMutate = pMutate;
        testCaseShuffleMutationFunction = new TestCaseShuffleMutationFunction(false);
        testCaseShuffleMutationFunction.setExecuteActions(false);
    }

    /**
     * Performs variation as described in the Sapienz paper with the one point cross over function,
     * see section 3.1 and Algorithm 2.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<TestSuite> mutate(IChromosome<TestSuite> chromosome) {
        return mutate(chromosome, new OnePointCrossOverFunction());
    }

    /**
     * Performs variation as described in the Sapienz paper, see section 3.1 and Algorithm 2.
     *
     * @param chromosome The chromosome to be mutated.
     * @param crossOverFunction The cross over function used in the mutation step.
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<TestSuite> mutate(IChromosome<TestSuite> chromosome,
                                         ICrossOverFunction<TestCase> crossOverFunction) {

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
                crossOverFunction.cross(wrap(t1, t2));
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

        // we need to execute those test cases that have been mutated
        for (TestCase testCase : testCases) {
            if (!notMutatedTestCases.contains(testCase)) {

                TestCase executed = TestCase.fromDummy(testCase);
                mutatedTestSuite.getTestCases().add(executed);

                if(Properties.SURROGATE_MODEL()) {
                    Registry.getUiAbstractionLayer().storeTraces();
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
     * Wraps two test cases into a test suite that is contained by a single chromosome in a list.
     *
     * @param t1 The first test case.
     * @param t2 The second test case.
     * @return A list of a single chromosome containing a test suite with two test cases.
     */
    private List<IChromosome<TestCase>> wrap(TestCase t1, TestCase t2) {
        List<IChromosome<TestCase>> wrappedTestCases = new ArrayList<>();

        IChromosome<TestCase> chromosomeT1 = new Chromosome<>(t1);
        IChromosome<TestCase> chromosomeT2 = new Chromosome<>(t2);

        wrappedTestCases.add(chromosomeT1);
        wrappedTestCases.add(chromosomeT2);

        return wrappedTestCases;
    }
}
