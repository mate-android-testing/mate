package org.mate.crash_reproduction.fitness;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BasicBlockDistance;
import org.mate.exploration.genetic.fitness.CallGraphDistance;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.model.TestCase;

import java.util.HashMap;
import java.util.List;

public class CrashDistance extends WeightedFitnessFunctions {

    private final List<String> targetStackTrace = Registry.getEnvironmentManager().getStackTrace();

    public CrashDistance() {
        super(new HashMap<IFitnessFunction<TestCase>, Double>(){{
            put(new CallGraphDistance<>(), 1D);
            put(new BasicBlockDistance<>(), 1D);
            put(new ReachedRequiredConstructor(), 1D);
        }});
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestCase> chromosome) {
        if (chromosome.getValue().reachedTarget(targetStackTrace)) {
            MATE.log("Was able to reproduce crash with " + chromosome.getValue().getId() + "!");
            ConditionalTerminationCondition.satisfiedCondition();
            return 0;
        }
        return super.getNormalizedFitness(chromosome);
    }
}
