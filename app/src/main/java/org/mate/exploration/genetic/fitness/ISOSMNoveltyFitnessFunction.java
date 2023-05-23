package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.Trace;

/**
 * The interface for SOSM-based novelty fitness functions.
 */
public interface ISOSMNoveltyFitnessFunction extends IFitnessFunction<TestCase> {

    @Override
    default double getFitness(final IChromosome<TestCase> chromosome) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    /**
     * A novelty fitness function is a maximising fitness function, i.e. the higher the novelty
     * the better.
     *
     * @return Returns {@code true} since novelty is to be maximised.
     */
    @Override
    default boolean isMaximizing() {
        return true;
    }

    @Override
    default double getNormalizedFitness(final IChromosome<TestCase> chromosome) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    /**
     * Retrieves the novelty of the given test case chromosome.
     *
     * @param chromosome The test case chromosome for which novelty should be evaluated.
     * @param trace Describes which transitions have been taken by the test case chromosome.
     * @return Returns the novelty for the given test case chromosome.
     */
    double getNovelty(final IChromosome<TestCase> chromosome, final Trace trace);
}
