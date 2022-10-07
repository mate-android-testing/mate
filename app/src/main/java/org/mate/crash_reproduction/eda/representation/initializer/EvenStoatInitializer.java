package org.mate.crash_reproduction.eda.representation.initializer;

import org.mate.crash_reproduction.eda.util.ProbabilityUtil;

import java.util.Map;

public class EvenStoatInitializer extends StoatProbabilityInitialization {

    public EvenStoatInitializer(double pPromisingAction) {
        super(pPromisingAction);
    }

    @Override
    protected <T> Map<T, Double> toProbabilities(Map<T, Double> weights) {
        return ProbabilityUtil.weightsToProbability(weights);
    }
}
