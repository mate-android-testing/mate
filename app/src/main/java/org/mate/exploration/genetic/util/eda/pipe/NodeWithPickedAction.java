package org.mate.exploration.genetic.util.eda.pipe;

import org.mate.interaction.action.Action;

import java.util.Map;
import java.util.Objects;

/**
 * Denotes a chosen node in the PPT.
 */
public class NodeWithPickedAction {

    /**
     * The action probabilities at the given node.
     */
    private final Map<Action, Double> actionProbabilities;

    /**
     * The chosen action at the given node.
     */
    public final Action action;

    /**
     * The action index.
     */
    public final int actionIndex;

    NodeWithPickedAction(Map<Action, Double> actionProbabilities, Action action, int actionIndex) {
        this.actionProbabilities = actionProbabilities;
        this.action = action;
        this.actionIndex = actionIndex;
    }

    /**
     * Retrieves the probability of the action at the given node.
     *
     * @return Returns the action probability.
     */
    public double getProbabilityOfAction() {
        return actionProbabilities.get(action);
    }

    /**
     * Sets the probability of the action at the given node.
     *
     * @param newProb The new action probability.
     */
    public void setProbabilityOfAction(double newProb) {
        actionProbabilities.put(action, newProb);
    }

    /**
     * Retrieves the action probabilities of the given node.
     *
     * @return Returns the action probabilities of the given node.
     */
    public Map<Action, Double> getActionProbabilities() {
        return actionProbabilities;
    }

    /**
     * Checks whether two nodes are equal.
     *
     * @param o The other node.
     * @return Returns {@code true} if the two nodes are equal, otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final NodeWithPickedAction that = (NodeWithPickedAction) o;
        return Objects.equals(actionProbabilities, that.actionProbabilities)
                && Objects.equals(action, that.action);
    }

    /**
     * Computes the hash code of the node.
     *
     * @return Returns the hash code associated with the node.
     */
    @Override
    public int hashCode() {
        return Objects.hash(actionProbabilities, action);
    }
}
