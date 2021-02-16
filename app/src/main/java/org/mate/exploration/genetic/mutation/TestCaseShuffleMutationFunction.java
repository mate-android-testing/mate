package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.interaction.action.Action;
import org.mate.utils.Randomness;

import java.util.Arrays;
import java.util.List;

public class TestCaseShuffleMutationFunction implements IMutationFunction<TestCase> {

    private boolean executeActions;

    public TestCaseShuffleMutationFunction(boolean executeActions) {
        this.executeActions = executeActions;
    }

    @Override
    public List<IChromosome<TestCase>> mutate(IChromosome<TestCase> chromosome) {
        if (executeActions) {
            throw new UnsupportedOperationException("Not implemented yet");
        } else {
            List<Action> actions =  chromosome.getValue().getEventSequence();
            Randomness.shuffleList(actions);
            TestCase mutatedTestCase = TestCase.newDummy();
            mutatedTestCase.getEventSequence().addAll(actions);
            IChromosome<TestCase> mutatedChromosome = new Chromosome<>(mutatedTestCase);
            return Arrays.asList(mutatedChromosome);
        }
    }

    public void setExecuteActions(boolean executeActions) {
        this.executeActions = executeActions;
    }
}
