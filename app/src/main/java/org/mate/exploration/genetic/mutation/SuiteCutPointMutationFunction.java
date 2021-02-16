package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.coverage.CoverageUtils;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuiteCutPointMutationFunction implements IMutationFunction<TestSuite> {
    public static final String MUTATION_FUNCTION_ID = "suite_cut_point_mutation_function";

    private final CutPointMutationFunction cutPointMutationFunction;

    public SuiteCutPointMutationFunction(int maxNumEvents) {
        cutPointMutationFunction = new CutPointMutationFunction(maxNumEvents);
        cutPointMutationFunction.setTestSuiteExecution(true);
    }

    @Override
    public List<IChromosome<TestSuite>> mutate(IChromosome<TestSuite> chromosome) {
        int randomElementIndex = Randomness.getRnd().nextInt(
                chromosome.getValue().getTestCases().size());
        TestSuite mutatedTestSuite = new TestSuite();
        IChromosome<TestSuite> mutatedChromosome = new Chromosome<>(mutatedTestSuite);

        List<TestCase> copyCoverageDataFor = new ArrayList<>(chromosome.getValue().getTestCases());
        copyCoverageDataFor.remove(randomElementIndex);

        if (!copyCoverageDataFor.isEmpty()) {
            CoverageUtils.copyCoverageData(chromosome, mutatedChromosome, copyCoverageDataFor);
            FitnessUtils.copyFitnessData(chromosome, mutatedChromosome, copyCoverageDataFor);
        }

        //Todo: handle coverage
        for (int i = 0; i < chromosome.getValue().getTestCases().size(); i++) {
            if (i == randomElementIndex) {
                TestCase mutatedTestCase = cutPointMutationFunction.mutate(new Chromosome<>(
                        chromosome.getValue().getTestCases().get(i))).get(0).getValue();
                mutatedTestSuite.getTestCases().add(mutatedTestCase);

                FitnessUtils.storeTestSuiteChromosomeFitness(mutatedChromosome, mutatedTestCase.toString());
                CoverageUtils.storeTestSuiteChromosomeCoverage(mutatedChromosome, mutatedTestCase.toString());
                CoverageUtils.logChromosomeCoverage(mutatedChromosome);
            } else {
                mutatedTestSuite.getTestCases().add(chromosome.getValue().getTestCases().get(i));
            }
        }
        return Arrays.asList(mutatedChromosome);
    }
}
