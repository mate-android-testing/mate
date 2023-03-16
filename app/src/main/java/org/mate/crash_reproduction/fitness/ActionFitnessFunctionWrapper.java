package org.mate.crash_reproduction.fitness;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;
import org.mate.utils.ChromosomeUtils;

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
        return getFitnessAfterXActions(chromosome, chromosome.getValue().getActionSequence().size());
    }

    public void recordCurrentActionFitness(IChromosome<TestCase> chromosome) {
        double fitness = fitnessFunction.getNormalizedFitness(chromosome);
        actionFitnessValues.put(ChromosomeUtils.getActionEntityId(chromosome), fitness);

        MATE.log("Testcase fitness after " + chromosome.getValue().getActionSequence().size() + " actions is: " + fitness);
    }

    public double getFitnessAfterXActions(IChromosome<TestCase> testCase, int actions) {
        return Objects.requireNonNull(actionFitnessValues.get(ChromosomeUtils.getActionEntityId(testCase, actions)));
    }
}
