package org.mate.exploration.genetic.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;

public class CallGraphDistance<T> implements IFitnessFunction<T> {

    @Override
    public double getFitness(IChromosome<T> chromosome) {
        return Registry.getEnvironmentManager().getCallTreeDistance(chromosome);
    }

    @Override
    public boolean isMaximizing() {
        return false;
    }

    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome);
    }
}
