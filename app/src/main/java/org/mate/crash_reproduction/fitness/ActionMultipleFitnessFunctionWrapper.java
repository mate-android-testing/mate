package org.mate.crash_reproduction.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;
import org.mate.utils.ChromosomeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActionMultipleFitnessFunctionWrapper extends ActionFitnessFunctionWrapper implements IMultipleFitnessFunctions<TestCase> {

    private final IMultipleFitnessFunctions<TestCase> multipleFitnessFunctions;
    private final Map<IFitnessFunction<TestCase>, Map<String, Double>> actionFitnessValuesPerFunction = new HashMap<>();

    public ActionMultipleFitnessFunctionWrapper(IFitnessFunction<TestCase> fitnessFunction) {
        super(fitnessFunction);
        this.multipleFitnessFunctions = IMultipleFitnessFunctions.wrapIfNecessary(fitnessFunction);
    }

    @Override
    public void recordCurrentActionFitness(IChromosome<TestCase> chromosome) {
        super.recordCurrentActionFitness(chromosome);
        for (IFitnessFunction<TestCase> fitnessFunction : multipleFitnessFunctions.getInnerFitnessFunction()) {
            actionFitnessValuesPerFunction.computeIfAbsent(fitnessFunction, f -> new HashMap<>())
                    .put(ChromosomeUtils.getActionEntityId(chromosome), fitnessFunction.getNormalizedFitness(chromosome));
        }
    }

    @Override
    public Set<IFitnessFunction<TestCase>> getInnerFitnessFunction() {
        return multipleFitnessFunctions.getInnerFitnessFunction();
    }
}
