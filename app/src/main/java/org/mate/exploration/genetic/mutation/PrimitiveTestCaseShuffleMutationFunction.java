package org.mate.exploration.genetic.mutation;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrimitiveTestCaseShuffleMutationFunction implements IMutationFunction<TestCase> {
    public static final String MUTATION_FUNCTION_ID = "primitive_test_case_shuffle_mutation_function";
    private boolean storeCoverage;

    public PrimitiveTestCaseShuffleMutationFunction() {
        this(Properties.STORE_COVERAGE);
    }

    public PrimitiveTestCaseShuffleMutationFunction(boolean storeCoverage) {
        this.storeCoverage = storeCoverage;
    }

    @Override
    public List<IChromosome<TestCase>> mutate(IChromosome<TestCase> chromosome) {
        ArrayList<Action> actions = new ArrayList<>(chromosome.getValue().getEventSequence());
        Randomness.shuffleList(actions);
        TestCase testCase = TestCase.newDummy();
        testCase.getEventSequence().addAll(actions);

        TestCase executedTestCase = TestCase.fromDummy(testCase);
        IChromosome<TestCase> mutatedChromosome = new Chromosome<>(executedTestCase);

        if (storeCoverage) {
            EnvironmentManager.storeCoverageData(chromosome, null);

            MATE.log_acc("After primitive test case shuffle mutation:");
            MATE.log_acc("Coverage of: " + chromosome.toString() + ": " + EnvironmentManager
                    .getCoverage(chromosome));
            MATE.log_acc("Found crash: " + String.valueOf(chromosome.getValue().getCrashDetected()));

            //TODO: remove hack, when better solution implemented
            LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);
        }

        return Arrays.asList(mutatedChromosome);
    }
}
