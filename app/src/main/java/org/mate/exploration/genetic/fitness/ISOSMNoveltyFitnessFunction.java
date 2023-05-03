package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.Trace;

public interface ISOSMNoveltyFitnessFunction extends IFitnessFunction<TestCase> {

    @Override
    default double getFitness(final IChromosome<TestCase> chromosome) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    @Override
    default boolean isMaximizing() {
        return true;
    }

    @Override
    default double getNormalizedFitness(final IChromosome<TestCase> chromosome) {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    // TODO: Add documentation.
    double getNovelty(final IChromosome<TestCase> chromosome, final Trace trace);
}
