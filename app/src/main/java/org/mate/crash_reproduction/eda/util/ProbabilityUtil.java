package org.mate.crash_reproduction.eda.util;

import java.util.HashMap;
import java.util.Map;

public class ProbabilityUtil {
    public static <T, W extends Number> Map<T, Double> weightsToProbability(Map<T, W> weights) {
        Map<T, Double> probabilities = new HashMap<>();
        double sum = weights.values().stream().mapToDouble(Number::doubleValue).sum();

        for (Map.Entry<T, W> weightEntry : weights.entrySet()) {
            probabilities.put(weightEntry.getKey(), weightEntry.getValue().doubleValue() / sum);
        }

        return probabilities;
    }
}
