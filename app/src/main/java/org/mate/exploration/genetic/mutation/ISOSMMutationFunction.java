package org.mate.exploration.genetic.mutation;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.Trace;

/**
 * The interface for SOSM-based mutation functions.
 */
public interface ISOSMMutationFunction extends IMutationFunction<TestCase> {

    @Override
    default IChromosome<TestCase> mutate(IChromosome<TestCase> chromosome) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    /**
     * Mutates the given test case chromosome.
     *
     * @param chromosome The test cases chromosome that should be mutated.
     * @param trace Describes which transitions have been taken by the test case chromosome.
     * @return Returns the mutated test case chromosome.
     */
    IChromosome<TestCase> mutate(IChromosome<TestCase> chromosome, Trace trace);
}
