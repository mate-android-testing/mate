package org.mate.exploration.genetic.util.eda.pipe;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.genetic.util.eda.IProbabilisticModel;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides an iterator over the nodes in the PPT described by a test case.
 */
class TestCaseModelIterator implements Iterator<NodeWithPickedAction> {

    /**
     * The probabilistic model (PPT).
     */
    private final IProbabilisticModel<TestCase> probabilisticModel;

    /**
     * An iterator over the action sequence of the test case.
     */
    private final Iterator<Action> actionIterator;

    /**
     * An iterator over the screen state sequence of the test case.
     */
    private final Iterator<IScreenState> stateIterator;

    /**
     * The position of the iterator.
     */
    private int returnedNodes = 0;

    /**
     * Initialises a new test case iterator over the probabilistic model.
     *
     * @param probabilisticModel The probabilistic model.
     * @param testCase The test case for which the iterator in the PPT should be constructed.
     */
    TestCaseModelIterator(IProbabilisticModel<TestCase> probabilisticModel, TestCase testCase) {

        this.probabilisticModel = probabilisticModel;
        this.actionIterator = testCase.getActionSequence().iterator();
        this.stateIterator = testCase.getStateSequence().stream()
                .map(stateId -> Registry.getUiAbstractionLayer().getGuiModel().getScreenStateById(stateId))
                .iterator();

        MATE.log_debug("PPT: ");
        MATE.log_debug(probabilisticModel.toString());

        // Reset cursor to root node of PPT.
        probabilisticModel.resetPosition();

        // This skips the root node.
        if (!stateIterator.next().equals(probabilisticModel.getState())) {
            MATE.log_warn("Test case does not start at root node...");
        }

    }

    /**
     * Checks whether the test case iterator has a further action.
     *
     * @return Returns {@code true} if the iterator is not exhausted, otherwise {@code false} is returned.
     */
    @Override
    public boolean hasNext() {
        return actionIterator.hasNext();
    }

    /**
     * Retrieves the next node of the PPT described by the test case.
     *
     * @return Returns the next node of the test case iterator.
     */
    @Override
    public NodeWithPickedAction next() {

        if (!hasNext()) {
            throw new NoSuchElementException("Forgot to call hasNext()?");
        }

        final Action currentAction = actionIterator.next();
        final NodeWithPickedAction nodeWithPickedAction
                = new NodeWithPickedAction(probabilisticModel.getActionProbabilities(),
                currentAction, returnedNodes);

        // Move the cursor in the PPT to the next state.
        if (stateIterator.hasNext()) {
            final IScreenState nextState = stateIterator.next();
            probabilisticModel.updatePositionImmutable(nextState);
        }

        returnedNodes++;

        return nodeWithPickedAction;
    }

}
