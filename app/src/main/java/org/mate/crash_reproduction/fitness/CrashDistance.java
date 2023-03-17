package org.mate.crash_reproduction.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BasicBlockDistance;
import org.mate.exploration.genetic.fitness.CallGraphDistance;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;

import java.util.HashMap;

public class CrashDistance extends WeightedFitnessFunctions {

    public CrashDistance() {
        super(new HashMap<IFitnessFunction<TestCase>, Double>(){{
            put(new CallGraphDistance<>(), 1D);
            put(new BasicBlockDistance<>(), 1D);
            put(new ReachedRequiredConstructor(), 1D);
        }});
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        return super.getNormalizedFitness(chromosome);
    }
}
