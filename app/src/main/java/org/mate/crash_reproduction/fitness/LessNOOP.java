package org.mate.crash_reproduction.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

public class LessNOOP implements IFitnessFunction<TestCase> {
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
        double noopActions = 0;
        double actions = 0;

        IScreenState prevState = null;
        for (IScreenState state : chromosome.getValue().getStateSequence()) {
            if (prevState != null) {
                actions++;

                if (prevState.equals(state)) {
                    noopActions++;
                }
            }
            prevState = state;
        }

        return actions == 0 ? 0 : (noopActions / actions);
    }
}
