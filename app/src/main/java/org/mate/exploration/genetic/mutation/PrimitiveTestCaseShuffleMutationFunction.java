package org.mate.exploration.genetic.mutation;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.interaction.action.Action;
import org.mate.utils.coverage.CoverageUtils;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrimitiveTestCaseShuffleMutationFunction implements IMutationFunction<TestCase> {
    public static final String MUTATION_FUNCTION_ID = "primitive_test_case_shuffle_mutation_function";

    @Override
    public List<IChromosome<TestCase>> mutate(IChromosome<TestCase> chromosome) {
        ArrayList<Action> actions = new ArrayList<>(chromosome.getValue().getEventSequence());
        Randomness.shuffleList(actions);
        TestCase testCase = TestCase.newDummy();
        testCase.getEventSequence().addAll(actions);

        TestCase executedTestCase = TestCase.fromDummy(testCase);
        IChromosome<TestCase> mutatedChromosome = new Chromosome<>(executedTestCase);

        FitnessUtils.storeTestCaseChromosomeFitness(mutatedChromosome);
        CoverageUtils.storeTestCaseChromosomeCoverage(mutatedChromosome);
        CoverageUtils.logChromosomeCoverage(mutatedChromosome);

        MATE.log_acc("Found crash: " + chromosome.getValue().getCrashDetected());

        return Arrays.asList(mutatedChromosome);
    }
}
