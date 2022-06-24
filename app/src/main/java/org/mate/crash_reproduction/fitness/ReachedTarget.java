package org.mate.crash_reproduction.fitness;

import org.mate.Registry;
import org.mate.crash_reproduction.CrashReproduction;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;

import java.util.Set;

public class ReachedTarget implements IFitnessFunction<TestCase> {
    private final Set<String> targets = Registry.getEnvironmentManager().getTargetActivities();

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return getNormalizedFitness(chromosome);
    }

    @Override
    public boolean isMaximizing() {
        return false;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return chromosome.getValue().getStateSequence().stream().anyMatch(state -> CrashReproduction.reachedTarget(targets, state))
                ? 0
                : 1;
    }
}
