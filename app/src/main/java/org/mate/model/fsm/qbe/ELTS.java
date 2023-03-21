package org.mate.model.fsm.qbe;

import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
import org.mate.model.TestCase;
import org.mate.model.fsm.FSM;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;
import org.mate.state.IScreenState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines an Extended Labeled Transition System (ELTS) as described on page 107/108 in the paper
 * "QBE: QLearning-Based Exploration of Android Applications".
 */
public class ELTS extends FSM {

    /**
     * The set of all actions (input alphabet) Z.
     */
    private final Set<Action> actions;

    /**
     * Whether the ELTS is deterministic or not.
     */
    private boolean deterministic;

    /**
     * Creates a new ELTS with an initial start state.
     *
     * @param root The start or root state.
     * @param packageName The package name of the AUT.
     */
    public ELTS(State root, String packageName) {
        super(root, packageName);
        actions = new HashSet<>();
        deterministic = true;
    }

    /**
     * Updates the model with a new transition. Keeps track of all actions seen so far and whether
     * the model is still deterministic.
     *
     * @param transition The new transition.
     */
    @Override
    public void addTransition(Transition transition) {

        QBETransition qbeTransition = (QBETransition) transition;
        transitions.add(qbeTransition);

        if (qbeTransition.getActionResult() != ActionResult.FAILURE_APP_CRASH) {
            // the AUT isn't crashed
            QBEState target = (QBEState) qbeTransition.getTarget();
            states.add(target);
            actions.addAll(target.getActions());
            deterministic = isDeterministic(qbeTransition);

            // TODO: Is the current state dependent on the passive learning?
            currentState = target;
        }
    }

    // TODO: Document and implement.
    @Override
    public State getState(IScreenState screenState) {
        // TODO: Perform here state equivalence check as outlined in equation (1) on page 108.
        throw new UnsupportedOperationException("not yet implemented!");
    }

    /**
     * Determines whether the ELTS is deterministic or not.
     *
     * @return Returns {@code true} if the ELTS is deterministic, otherwise {@code false} is returned.
     */
    public boolean isDeterministic() {
        return deterministic;
    }

    // TODO: Document and implement.
    private boolean isDeterministic(final QBETransition transition) {
        throw new UnsupportedOperationException("not yet implemented!");
    }

    // TODO: Document and implement.
    public void passiveLearn(final List<TestCase> testCases, final TestCase currentTestCase) {
        throw new UnsupportedOperationException("not yet implemented!");
    }
}
