package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.FitnessUtils;
import org.mate.commons.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a cut point mutation function for {@link TestSuite}s.
 */
public class SuiteCutPointMutationFunction implements IMutationFunction<TestSuite> {

    /**
     * The cut point mutation function for the individual test cases.
     */
    private final CutPointMutationFunction cutPointMutationFunction;

    /**
     * Initialises the cut point mutation function.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public SuiteCutPointMutationFunction(int maxNumEvents) {
        cutPointMutationFunction = new CutPointMutationFunction(maxNumEvents);
        cutPointMutationFunction.setTestSuiteExecution(true);
    }

    /**
     * Performs a cut point mutation on the given test suite, i.e. a random cut point is selected
     * and that particular test case is mutated with the cut point mutation function for test cases.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<TestSuite> mutate(IChromosome<TestSuite> chromosome) {

        // choose a random cut point
        int randomElementIndex = Randomness.getRnd().nextInt(
                chromosome.getValue().getTestCases().size());

        TestSuite mutatedTestSuite = new TestSuite();
        IChromosome<TestSuite> mutatedChromosome = new Chromosome<>(mutatedTestSuite);

        // copy the coverage data for all test cases except the one defined through the cut point
        List<TestCase> copyCoverageDataFor = new ArrayList<>(chromosome.getValue().getTestCases());
        copyCoverageDataFor.remove(randomElementIndex);

        if (!copyCoverageDataFor.isEmpty()) {
            CoverageUtils.copyCoverageData(chromosome, mutatedChromosome, copyCoverageDataFor);
            FitnessUtils.copyFitnessData(chromosome, mutatedChromosome, copyCoverageDataFor);
        }

        for (int i = 0; i < chromosome.getValue().getTestCases().size(); i++) {

            if (i == randomElementIndex) {
                // perform a cut point mutation on the chosen test case
                TestCase mutatedTestCase = cutPointMutationFunction.mutate(new Chromosome<>(
                        chromosome.getValue().getTestCases().get(i))).getValue();
                mutatedTestSuite.getTestCases().add(mutatedTestCase);

                FitnessUtils.storeTestSuiteChromosomeFitness(mutatedChromosome, mutatedTestCase);
                CoverageUtils.storeTestSuiteChromosomeCoverage(mutatedChromosome, mutatedTestCase);
                CoverageUtils.logChromosomeCoverage(mutatedChromosome);
            } else {
                mutatedTestSuite.getTestCases().add(chromosome.getValue().getTestCases().get(i));
            }
        }

        return mutatedChromosome;
    }
}
