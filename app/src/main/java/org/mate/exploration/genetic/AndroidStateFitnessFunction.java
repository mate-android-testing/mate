package org.mate.exploration.genetic;

import org.mate.model.TestCase;

public class AndroidStateFitnessFunction implements IFitnessFunction<TestCase> {
    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return chromosome.getValue().getVisitedStates().size();
    }
}
