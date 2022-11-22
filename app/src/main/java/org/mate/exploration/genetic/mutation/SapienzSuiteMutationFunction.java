package org.mate.exploration.genetic.mutation;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a mutation operator as defined in the Sapienz paper, see
 * https://discovery.ucl.ac.uk/id/eprint/1508043/1/p_issta16_sapienz.pdf. In particular, have
 * a look at section 3.1 and Algorithm 2.
 */
public class SapienzSuiteMutationFunction implements IMutationFunction<List<TestSuite>> {

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
     * Performs variation as described in the Sapienz paper with the one point cross over function,
     * see section 3.1 and Algorithm 2.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<List<TestSuite>> mutate(IChromosome<List<TestSuite>> chromosome) {
        TestSuite original = chromosome.getValue().get(0);
        TestSuite shuffled = chromosome.getValue().get(1);
        TestSuite crossOver = chromosome.getValue().get(2);

        TestSuite mutatedTestSuite = new TestSuite();
        IChromosome<TestSuite> mutatedChromosome = new Chromosome<>(mutatedTestSuite);
        List<TestSuite> mutatedList = new ArrayList<>();
        mutatedList.add(mutatedTestSuite);

        List<TestCase> shuffledTestCases = shuffled.getTestCases();
        List<TestCase> crossOverTestCases = crossOver.getTestCases();
        List<TestCase> notMutatedTestCases = new LinkedList<>();

        for (int i = 1; i < shuffledTestCases.size(); i = i + 2) {
            double rnd = Randomness.getRnd().nextDouble();

            if (rnd < pMutate) { // if r < q
                /*
                 * Sapienz performs a one-point crossover on two neighbouring test cases. Since
                 * MATE only supports crossover functions that return a single offspring, we make
                 * the one-point crossover here in place.
                 */
                TestCase t1 = crossOverTestCases.get(i - 1);
                TestCase t2 = crossOverTestCases.get(i);

                shuffledTestCases.set((i - 1), t1);
                shuffledTestCases.set(i, t2);
            } else {
                TestCase t1 = shuffledTestCases.get(i - 1);
                TestCase t2 = shuffledTestCases.get(i);

                notMutatedTestCases.add(t1);
                notMutatedTestCases.add(t2);
            }
        }

        for (int i = 0; i < shuffledTestCases.size(); i++) {
            double rnd = Randomness.getRnd().nextDouble();

            TestCase testCase = shuffledTestCases.get(i);

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
        for (TestCase testCase : shuffledTestCases) {
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
                CoverageUtils.copyCoverageData(new Chromosome<>(original), mutatedChromosome, notMutatedTestCases);
            }
            FitnessUtils.copyFitnessData(new Chromosome<>(original), mutatedChromosome, notMutatedTestCases);
        }

        CoverageUtils.logChromosomeCoverage(mutatedChromosome);
        return new Chromosome<>(mutatedList);
    }
}
