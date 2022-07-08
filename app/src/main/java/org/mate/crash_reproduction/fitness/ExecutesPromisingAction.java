package org.mate.crash_reproduction.fitness;

import org.mate.Registry;
import org.mate.crash_reproduction.CrashReproduction;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Iterator;
import java.util.Set;

public class ExecutesPromisingAction implements IFitnessFunction<TestCase> {
    private final UIAbstractionLayer uiAbstractionLayer = Registry.getUiAbstractionLayer();
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
        boolean executesPromisingAction = false;
        boolean executesPromisingActionOnTarget = false;

        Iterator<IScreenState> stateIterator = chromosome.getValue().getStateSequence().iterator();
        for (Action action : chromosome.getValue().getEventSequence()) {
            IScreenState executedOn = stateIterator.next();

            if (uiAbstractionLayer.getPromisingActions(executedOn).contains(action)) {
                executesPromisingAction = true;

                if (CrashReproduction.reachedTarget(targets, executedOn)) {
                    executesPromisingActionOnTarget = true;
                }
            }
        }

        return executesPromisingActionOnTarget
                ? 0
                : executesPromisingAction ? 0.5 : 1;
    }
}
