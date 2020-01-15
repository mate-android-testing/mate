package org.mate.utils;

import org.mate.MATE;
import org.mate.model.TestCase;
import org.mate.ui.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides different optimisation strategies for a {@link org.mate.model.TestCase}.
 * This can be essentially anything from removing all actions of kind
 * {@link org.mate.interaction.intent.IntentBasedAction} to dropping solely individual
 * actions.
 */
public final class TestCaseOptimizer {

    private TestCaseOptimizer() {
        throw new UnsupportedOperationException("Utility class not instantiable!");
    }

    /**
     * Removes from the given test case the last {@param n} actions of type {@param actionType}.
     *
     * @param testCase The test case to be optimised.
     * @param n The number of intent-based actions that should be removed (upper bound), n > 0.
     * @param actionType The type of action, either an intent-based action, a system action
     *                   or a UI action.
     * @return Returns the test case after applied the optimisation.
     */
    public static TestCase removeLastActions(TestCase testCase, int n, Class actionType) {

        MATE.log("Optimising TestCase!");

        List<Action> actions = new ArrayList<>(testCase.getEventSequence());
        Collections.reverse(actions);

        List<Action> toBeRemoved = new ArrayList<>(n);
        int ctr = 0;

        for (Action action : actions) {

            if (ctr == n) {
                // we reached the number of elements we want to remove
                break;
            }

            if (action.getClass().equals(actionType)) {
                toBeRemoved.add(action);
                ctr++;
            }
        }

        MATE.log("Removed from TestCase " + ctr + " " + actionType.getName() + "!");

        // remove the actions from the test case
        testCase.getEventSequence().removeAll(toBeRemoved);
        return testCase;
    }

}
