package org.mate.crash_reproduction.eda.representation;

import org.mate.crash_reproduction.eda.util.ProbabilityUtil;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NewStateRepresentationWrapper implements IModelRepresentation {
    private final IModelRepresentation internalRepresentation;

    public NewStateRepresentationWrapper(IModelRepresentation internalRepresentation) {
        this.internalRepresentation = internalRepresentation;
    }

    @Override
    public ModelRepresentationIterator getIterator() {
        return new NewStateRepresentationIterator();
    }

    @Override
    public void resetProbabilities() {
        internalRepresentation.resetProbabilities();
    }

    @Override
    public Iterator<NodeWithPickedAction> getTestcaseIterator(TestCase testCase) {
        List<NodeWithPickedAction> nodes = new LinkedList<>();
        new TestCaseModelIterator(getIterator(), testCase).forEachRemaining(nodes::add);

        nodes.removeIf(node -> !node.getActionProbabilities().containsKey(node.action));

        return nodes.iterator();
    }

    @Override
    public String toString() {
        return internalRepresentation.toString();
    }

    private class NewStateRepresentationIterator implements ModelRepresentationIterator {
        private final ModelRepresentationIterator internalIterator = internalRepresentation.getIterator();

        @Override
        public Map<Action, Double> getActionProbabilities() {
            return internalIterator.getActionProbabilities();
        }

        @Override
        public IScreenState getState() {
            return internalIterator.getState();
        }

        @Override
        public void updatePosition(TestCase testCase, Action action, IScreenState currentScreenState) {
            if (!internalIterator.getState().equals(currentScreenState)) {
                internalIterator.updatePosition(testCase, action, currentScreenState);
            } else {
                Map<Action, Double> probabilities = getActionProbabilities();
                probabilities.remove(action);

                Map<Action, Double> normalizedProb = ProbabilityUtil.weightsToProbability(probabilities);
                probabilities.clear();
                probabilities.putAll(normalizedProb);
            }
        }

        @Override
        public void updatePositionImmutable(IScreenState currentScreenState) {
            if (!internalIterator.getState().equals(currentScreenState)) {
                internalIterator.updatePositionImmutable(currentScreenState);
            }
        }
    }
}
