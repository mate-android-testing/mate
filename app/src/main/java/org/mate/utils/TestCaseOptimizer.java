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

    /**
     * Removes the action at the specified {@param index}.
     *
     * @param testCase The given test case.
     * @param index The index of the action which should be removed.
     * @return Returns the test case without the action at the given index.
     */
    public static TestCase removeActionAtIndex(TestCase testCase, int index) {
        testCase.getEventSequence().remove(index);
        return testCase;
    }

    /**
     * Removes the last action of a test case.
     *
     * @param testCase The given test case.
     * @return Returns the test case without the last action.
     */
    public static TestCase removeLastAction(TestCase testCase) {

        int lastIndex = testCase.getEventSequence().size() - 1;
        testCase.getEventSequence().remove(lastIndex);
        return testCase;
    }

    /**
     * Checks whether the last action of the given test case is of type {@param type}.
     *
     * @param testCase The given test case.
     * @param type The action type, e.g. {@link org.mate.interaction.intent.IntentBasedAction}.
     * @return Returns {@code true} if the last action matches the given action type,
     *          otherwise {@code false} is returned.
     */
    public static boolean isLastActionOfGivenType(TestCase testCase, Class type) {

        int lastIndex = testCase.getEventSequence().size() - 1;
        return testCase.getEventSequence().get(lastIndex).getClass().equals(type);
    }

    /**
     * Removes all actions of a test case except the last one. The idea is that
     * a {@link org.mate.interaction.intent.IntentBasedAction} may lead to a crash
     * directly without preceding UI or Intent-based actions. If no crash occurs anymore,
     * the last action didn't cause the crash, at least not independently.
     *
     * @param testCase The given test case.
     * @return Returns the test case containing solely the last action.
     */
    public static TestCase removeAllExceptLastAction(TestCase testCase) {

        List<Action> toBeRemoved = new ArrayList<>();

        for(int i=0; i<testCase.getEventSequence().size()-1; i++) {
            toBeRemoved.add(testCase.getEventSequence().get(i));
        }

        testCase.getEventSequence().removeAll(toBeRemoved);
        return testCase;
    }

}
