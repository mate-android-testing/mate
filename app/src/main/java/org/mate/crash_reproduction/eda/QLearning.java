package org.mate.crash_reproduction.eda;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class QLearning<S, A> {
    private final Map<S, Map<A, Double>> qTable = new HashMap<>();
    private final double initialQValue = 0D;
    private final double alpha;
    private final double gamma;

    public QLearning(double alpha, double gamma) {
        this.alpha = alpha;
        this.gamma = gamma;
    }

    public void update(S stateT, A actionT, S stateTp1, A actionTp1, double reward) {
        double qValueT = qTable.computeIfAbsent(stateT, a -> new HashMap<>()).computeIfAbsent(actionT, a -> initialQValue);
        double qValueTp1 = qTable.computeIfAbsent(stateTp1, a -> new HashMap<>()).computeIfAbsent(actionTp1, a -> initialQValue);
        double newQValueT = qValueT + alpha * (reward + gamma * qValueTp1 - qValueT);
        qTable.computeIfAbsent(stateT, a -> new HashMap<>()).put(actionT, newQValueT);
    }

    public Map<A, Double> getDistribution(S state) {
        Map<A, Double> qValues = qTable.computeIfAbsent(state, a -> new HashMap<>());

        double qValueSum = qValues.values().stream().mapToDouble(a -> a).sum();

        return qValues.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / qValueSum));
    }

    public Set<A> getPossibleAction(S state) {
        return qTable.computeIfAbsent(state, a -> new HashMap<>()).keySet();
    }

    public Map<A, Double> getBoltzmannDistribution(S state, int temperature) {
        return new BoltzmannDistribution(temperature).getDistribution(qTable.computeIfAbsent(state, a -> new HashMap<>()));
    }

    public interface DistributionCalculation<A> {
        Map<A, Double> getDistribution(Map<A, Double> qValues);
    }

    public class BoltzmannDistribution implements DistributionCalculation<A> {
        private final int temperature;

        public BoltzmannDistribution(int temperature) {
            this.temperature = temperature;
        }

        @Override
        public Map<A, Double> getDistribution(Map<A, Double> qValues) {
            double zT = normalization(qValues);
            return qValues.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> upper(entry.getValue()) / zT));
        }

        private double normalization(Map<A, Double> qValues) {
            return qValues.values().stream().mapToDouble(this::upper).sum();
        }

        private double upper(double qValue) {
            return Math.exp(qValue / temperature);
        }
    }
}
