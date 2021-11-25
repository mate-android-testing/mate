package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;

public class AndroidStateFitnessFunction implements IFitnessFunction<TestCase> {

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return chromosome.getValue().getVisitedStates().size();
    }

    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return getFitness(chromosome) / Double.MAX_VALUE;
    }
}
