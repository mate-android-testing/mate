package org.mate.exploration.genetic.mutation;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.Coverage;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuiteCutPointMutationFunction implements IMutationFunction<TestSuite> {
    public static final String MUTATION_FUNCTION_ID = "suite_cut_point_mutation_function";

    private final CutPointMutationFunction cutPointMutationFunction;

    public SuiteCutPointMutationFunction(int maxNumEvents) {
        cutPointMutationFunction = new CutPointMutationFunction(maxNumEvents);
    }

    @Override
    public List<IChromosome<TestSuite>> mutate(IChromosome<TestSuite> chromosome) {
        int randomElementIndex = Randomness.getRnd().nextInt(
                chromosome.getValue().getTestCases().size());
        TestSuite mutatedTestSuite = new TestSuite();
        IChromosome<TestSuite> mutatedChromosome = new Chromosome<>(mutatedTestSuite);

        List<TestCase> copyCoverageDataFor = new ArrayList<>(chromosome.getValue().getTestCases());
        copyCoverageDataFor.remove(randomElementIndex);
        Registry.getEnvironmentManager().copyCoverageData(chromosome, mutatedChromosome, copyCoverageDataFor);

        //Todo: handle coverage
        for (int i = 0; i < chromosome.getValue().getTestCases().size(); i++) {
            if (i == randomElementIndex) {
                TestCase mutatedTestCase = cutPointMutationFunction.mutate(new Chromosome<>(
                        chromosome.getValue().getTestCases().get(i))).get(0).getValue();
                mutatedTestSuite.getTestCases().add(mutatedTestCase);
                if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {

                    Registry.getEnvironmentManager().storeCoverageData(Properties.COVERAGE(),
                            mutatedChromosome.toString(), mutatedTestCase.toString());

                    MATE.log_acc("Coverage of: " + mutatedChromosome.toString() + ": "
                            +Registry.getEnvironmentManager().getCoverage(Properties.COVERAGE(),
                            mutatedChromosome.toString()));
                    MATE.log_acc("Found crash: " + mutatedTestCase.getCrashDetected());
                }
            } else {
                mutatedTestSuite.getTestCases().add(chromosome.getValue().getTestCases().get(i));
            }
        }
        return Arrays.asList(mutatedChromosome);
    }
}
