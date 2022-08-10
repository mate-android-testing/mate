package org.mate.crash_reproduction.eda.representation;

import org.mate.interaction.action.Action;

import java.util.Map;
import java.util.Objects;

public class NodeWithPickedAction {
    private final Map<Action, Double> actionProbabilities;
    public final Action action;
    public final int actionIndex;

    NodeWithPickedAction(Map<Action, Double> actionProbabilities, Action action, int actionIndex) {
        this.actionProbabilities = actionProbabilities;
        this.action = action;
        this.actionIndex = actionIndex;
    }

    public double getProbabilityOfAction() {
        return actionProbabilities.get(action);
    }

    public void putProbabilityOfAction(double newProb) {
        actionProbabilities.put(action, newProb);
    }

    public Map<Action, Double> getActionProbabilities() {
        return actionProbabilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeWithPickedAction that = (NodeWithPickedAction) o;
        return Objects.equals(actionProbabilities, that.actionProbabilities) && Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionProbabilities, action);
    }
}
