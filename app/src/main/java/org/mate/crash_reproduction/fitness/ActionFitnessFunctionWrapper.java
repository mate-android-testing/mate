package org.mate.crash_reproduction.fitness;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActionFitnessFunctionWrapper implements IFitnessFunction<TestCase> {
    private final Map<String, Double> actionFitnessValues = new HashMap<>();
    private final IFitnessFunction<TestCase> fitnessFunction;

    public ActionFitnessFunctionWrapper(IFitnessFunction<TestCase> fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return getNormalizedFitness(chromosome);
    }

    @Override
    public boolean isMaximizing() {
        return fitnessFunction.isMaximizing();
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return getFitnessAfterXActions(chromosome, chromosome.getValue().getEventSequence().size());
    }

    public void recordCurrentActionFitness(IChromosome<TestCase> chromosome) {
        actionFitnessValues.put(Registry.getEnvironmentManager().getActionEntityId(chromosome), fitnessFunction.getNormalizedFitness(chromosome));
    }

    public double getFitnessAfterXActions(IChromosome<TestCase> testCase, int actions) {
        return Objects.requireNonNull(actionFitnessValues.get(Registry.getEnvironmentManager().getActionEntityId(testCase, actions)));
    }
}
