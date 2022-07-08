package org.mate.crash_reproduction.eda.representation;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Walks through the model tree like the testcase has before
 */
public class TestCaseModelIterator implements Iterator<TestCaseModelIterator.NodeWithPickedAction> {
    private final ModelRepresentationIterator representationIterator;
    private final Iterator<Action> actionIterator;
    private final Iterator<IScreenState> stateIterator;

    public TestCaseModelIterator(ModelRepresentationIterator representationIterator, TestCase testCase) {
        this.representationIterator = representationIterator;
        this.actionIterator = testCase.getEventSequence().iterator();
        this.stateIterator = testCase.getStateSequence().iterator();

        // This skips the root node
        if (!stateIterator.next().equals(representationIterator.getState())) {
            MATE.log_warn("Testcase does not start at root...");
        }
    }

    @Override
    public boolean hasNext() {
        return actionIterator.hasNext();
    }

    @Override
    public NodeWithPickedAction next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Forgot to call hasNext?");
        }

        Action currentAction = actionIterator.next();
        NodeWithPickedAction nodeWithPickedAction = new NodeWithPickedAction(representationIterator.getActionProbabilities(), currentAction);

        if (stateIterator.hasNext()) {
            IScreenState nextState = stateIterator.next();
            representationIterator.updatePositionImmutable(nextState);
        } else if (actionIterator.hasNext()) {
            throw new IllegalStateException("Number of actions should at most be off by one");
        }

        return nodeWithPickedAction;
    }

    public static class NodeWithPickedAction {
        private final Map<Action, Double> actionProbabilities;
        public final Action action;

        private NodeWithPickedAction(Map<Action, Double> actionProbabilities, Action action) {
            this.actionProbabilities = actionProbabilities;
            this.action = action;
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
}
