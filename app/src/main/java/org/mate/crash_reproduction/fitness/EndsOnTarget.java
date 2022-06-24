package org.mate.crash_reproduction.fitness;

import org.mate.Registry;
import org.mate.crash_reproduction.CrashReproduction;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Set;

public class EndsOnTarget implements IFitnessFunction<TestCase> {
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
        boolean reachedTarget = false;
        double actionsAfterTarget = 0;

        for (IScreenState state : chromosome.getValue().getStateSequence()) {
            boolean onTarget = CrashReproduction.reachedTarget(targets, state);

            if (onTarget) {
                actionsAfterTarget = 0;
                reachedTarget = true;
            } else if (reachedTarget) {
                // Left target
                actionsAfterTarget++;
            }
        }

        // Ideally the testcase ends on the target
        return reachedTarget
                ? actionsAfterTarget / chromosome.getValue().getEventSequence().size()
                : 1; // did not even reach target
    }
}
