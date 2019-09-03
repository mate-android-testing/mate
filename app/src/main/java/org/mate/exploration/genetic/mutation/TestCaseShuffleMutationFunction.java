package org.mate.exploration.genetic.mutation;

import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.ui.Action;
import org.mate.utils.Randomness;

import java.util.Arrays;
import java.util.List;

public class TestCaseShuffleMutationFunction implements IMutationFunction<TestCase> {
    private final boolean storeCoverage;
    private boolean executeActions;

    public TestCaseShuffleMutationFunction() {
        this(Properties.STORE_COVERAGE);
    }

    public TestCaseShuffleMutationFunction(boolean storeCoverage) {
        this.storeCoverage = storeCoverage;
    }

    @Override
    public List<IChromosome<TestCase>> mutate(IChromosome<TestCase> chromosome) {
        if (executeActions) {
            throw new UnsupportedOperationException("Not implemented yet");
        } else {
            List<Action> actions =  chromosome.getValue().getEventSequence();
            Randomness.shuffleList(actions);
            TestCase mutatedTestCase = new TestCase("dummy");
            mutatedTestCase.getEventSequence().addAll(actions);
            IChromosome<TestCase> mutatedChromosome = new Chromosome<>(mutatedTestCase);
            return Arrays.asList(mutatedChromosome);
        }
    }

    public void setExecuteActions(boolean executeActions) {
        this.executeActions = executeActions;
    }
}
