package org.mate.crash_reproduction.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;

public class ReachedRequiredConstructor implements IFitnessFunction<TestCase> {

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return getNormalizedFitness(chromosome);
    }

    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return Registry.getEnvironmentManager().getReachedRequiredConstructors(chromosome);
    }
}
