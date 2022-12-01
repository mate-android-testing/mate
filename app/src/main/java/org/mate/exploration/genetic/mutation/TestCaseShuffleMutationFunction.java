package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.utils.Randomness;

import java.util.List;

/**
 * Provides a shuffle mutation function for {@link TestCase}s.
 */
public class TestCaseShuffleMutationFunction implements IMutationFunction<TestCase> {

    /**
     * Whether to execute actions during mutation or not.
     */
    private boolean executeActions;

    /**
     * Initialises the test case shuffle mutation function.
     *
     * @param executeActions Whether to execute actions during mutation or not.
     */
    public TestCaseShuffleMutationFunction(boolean executeActions) {
        this.executeActions = executeActions;
    }

    /**
     * Performs a test case shuffle mutation. In general, the actions can't be executed since
     * they are associated with a widget on a particular activity.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<TestCase> mutate(IChromosome<TestCase> chromosome) {
        /*
        * TODO: Provide a more sophisticated shuffle mutation that can be actually executed, otherwise
        *  this operation is a duplicate of its primitive counter part and can be removed.
         */
        if (executeActions) {
            throw new UnsupportedOperationException("Not implemented yet!");
        } else {
            List<Action> actions =  chromosome.getValue().getActionSequence();
            Randomness.shuffleList(actions);
            TestCase mutatedTestCase = TestCase.newDummy();
            mutatedTestCase.getActionSequence().addAll(actions);
            return new Chromosome<>(mutatedTestCase);
        }
    }

    /**
     * Controls whether the actions should be executed during mutation.
     *
     * @param executeActions Whether to execute actions or not.
     */
    public void setExecuteActions(boolean executeActions) {
        this.executeActions = executeActions;
    }
}
