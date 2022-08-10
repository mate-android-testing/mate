package org.mate.crash_reproduction.eda.representation;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Walks through the model tree like the testcase has before
 */
class TestCaseModelIterator implements Iterator<NodeWithPickedAction> {
    private final ModelRepresentationIterator representationIterator;
    private final Iterator<Action> actionIterator;
    private final Iterator<IScreenState> stateIterator;
    private int returnedNodes = 0;

    TestCaseModelIterator(ModelRepresentationIterator representationIterator, TestCase testCase) {
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
        NodeWithPickedAction nodeWithPickedAction = new NodeWithPickedAction(representationIterator.getActionProbabilities(), currentAction, returnedNodes);

        if (stateIterator.hasNext()) {
            IScreenState nextState = stateIterator.next();
            representationIterator.updatePositionImmutable(nextState);
        } else if (actionIterator.hasNext()) {
            throw new IllegalStateException("Number of actions should at most be off by one");
        }

        returnedNodes++;

        return nodeWithPickedAction;
    }

}
