package org.mate.model.fsm.qbe;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import org.mate.Properties;
import org.mate.interaction.action.Action;
import org.mate.model.fsm.FSM;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.utils.MathUtils;
import org.mate.utils.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Defines an Extended Labeled Transition System (ELTS) as described on page 107/108 in the paper
 * "QBE: QLearning-Based Exploration of Android Applications".
 */
public class ELTS extends FSM {

    public static final double COSINE_SIMILARITY_THRESHOLD = Properties.COSINE_SIMILARITY_THRESHOLD();

    /**
     * Represents the virtual root state. In a deterministic ELTS, this state should only ever have
     * a single neighbour, which is the actual initial state of the AUT.
     */
    public static final QBEState VIRTUAL_ROOT_STATE = new QBEState(-1, ScreenStateFactory.newDummyState());

    /**
     * QBE requires state which represents a crash, such that this state is part of the serialized
     * transition system.
     */
    public static final QBEState CRASH_STATE = new QBEState(-2, ScreenStateFactory.newDummyState());

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
        QBEState target = (QBEState) qbeTransition.getTarget();
        states.add(target);
        actions.addAll(target.getActions());
        deterministic = isDeterministic(qbeTransition);
        currentState = target;
    }

    private Pair<List<Double>, List<Double>> featureMapToContentVectors(
            final Map<String, Integer> features1, final Map<String, Integer> features2) {

        final Set<String> keys = new HashSet<>(features1.size() + features2.size());
        keys.addAll(features1.keySet());
        keys.addAll(features2.keySet());

        final List<Double> vector1 = new ArrayList<>(keys.size());
        final List<Double> vector2 = new ArrayList<>(keys.size());

        for (final String key : keys) {
            vector1.add((double) features1.getOrDefault(key, 0));
            vector2.add((double) features2.getOrDefault(key, 0));
        }

        return new Pair<>(vector1, vector2);
    }

    private boolean isEquivalent(final QBEState s1, final QBEState s2) {
        final Pair<List<Double>, List<Double>> contentVectors = featureMapToContentVectors(s1.getFeatureMap(), s2.getFeatureMap());
        return MathUtils.cosineSimilarity(contentVectors.first, contentVectors.second)
                > COSINE_SIMILARITY_THRESHOLD;
    }

    @Override
    public State getState(IScreenState screenState) {
        if (screenState == VIRTUAL_ROOT_STATE.getScreenState()) {
            return VIRTUAL_ROOT_STATE;
        }

        if (screenState == CRASH_STATE.getScreenState()) {
            return CRASH_STATE;
        }

        final QBEState newState = new QBEState(nextStateId, screenState);
        for (final State s : states) {
            if (isEquivalent((QBEState) s, newState)) {
                return s;
            }
        }

        ++nextStateId;
        return newState;
    }

    /**
     * Determines whether the ELTS is deterministic or not.
     *
     * @return Returns {@code true} if the ELTS is deterministic, otherwise {@code false} is returned.
     */
    public boolean isDeterministic() {
        return deterministic;
    }

    /**
     * Determines whether is there a transitions which has the same source and action as the given
     * transition. If this is the case, then the ELTS is not deterministic.
     * <p>
     * Note: The ELTS is determinist if and only if the underlying FSM is deterministic.
     *
     * @param transition The transition to compare against.
     * @return Whether there is a transition with the same source and action as the given transition.
     */
    private boolean isDeterministic(final QBETransition transition) {
        return transitions.stream().noneMatch(
                t -> t.getSource().equals(transition.getSource())
                        && t.getAction().equals(transition.getAction())
                        && !transition.getTarget().equals(t.getTarget()));
    }

    /**
     * Determines whether the ELTS is deterministic.
     */
    private boolean checkIsDeterministic() {
        return transitions.stream().map(t -> (QBETransition) t).allMatch(this::isDeterministic);
    }

    /**
     * Removes the unreachable states.
     */
    private void removeUnreachableStates() {
        final Set<Transition> reachableTransitions = new HashSet<>(transitions.size());
        final Set<State> reachableStates = new HashSet<>(states.size());
        reachableStates.add(VIRTUAL_ROOT_STATE);

        boolean change;
        do {
            final Set<Transition> newTransitions = transitions.stream()
                    .filter(transitions -> reachableStates.contains(transitions.getSource()))
                    .collect(toSet());
            final Set<State> newStates = newTransitions.stream()
                    .map(Transition::getTarget)
                    .collect(toSet());
            change = reachableTransitions.addAll(newTransitions) || reachableStates.addAll(newStates);
        } while (change);

        states.retainAll(reachableStates);
        transitions.retainAll(reachableTransitions);
        // TODO: Figure out what to do with the actions.
        // actions.retainAll(reachableStates.stream().flatMap(s -> s.getActions().stream()).collect(toSet()));
    }


    /**
     * QBE assumes that the AUT behaves fully deterministically.
     * <p>
     * If this is the case, than the ELTS should be deterministic as well. However, due to the fuzzy
     * state equivalence definition, the inferred ELTS can become non deterministic, meaning that
     * the ELTS does not comply to the AUT.
     * This algorithms corrects a non-deterministic ELTS and turns it back into a deterministic one.
     * <p>
     * WARNING: Passive Learn can recuse infinitely, the AUT is non deterministic!
     *
     * @param testsuite                The tests that have been executed so far. The corrected ELTS should contain
     *                                 contain every path thats described by a test case.
     * @param nonDeterministicTestcase The test case that caused the ELTS to become non deterministic.
     * @return The (modified) test suite that compiles to the modified ELTS.
     */
    public List<List<QBETransition>> passiveLearn(List<List<QBETransition>> testsuite, final List<QBETransition> nonDeterministicTestcase) {
        assert !testsuite.contains(nonDeterministicTestcase)
                : "The testcase that causes the ELTS to become non deterministic should not be part of the test suite.";

        final int testcaseLength = nonDeterministicTestcase.size();
        if (testcaseLength == 1) {
            throw new AssertionError(
                    "The first action should always be deterministic," +
                            " if the AUT is deterministic.");

        } else {
            final QBETransition conflictingTransition
                    = nonDeterministicTestcase.remove(testcaseLength - 1);
            final QBETransition secondLastTransition
                    = nonDeterministicTestcase.get(testcaseLength - 2);


            final QBEState stateWithDummy = new QBEState((QBEState) secondLastTransition.getTarget());
            stateWithDummy.addDummyComponent();

            transitions.remove(conflictingTransition);
            transitions.remove(secondLastTransition);
            transitions.add(new QBETransition(secondLastTransition.getSource(), stateWithDummy, secondLastTransition.getAction(), secondLastTransition.getActionResult()));

            testsuite.add(nonDeterministicTestcase);

            testsuite = testsuite.stream().map(testcase -> testcase.stream()
                    .map(tr -> tr.getSource().equals(secondLastTransition.getSource())
                            && Objects.equals(tr.getTarget(), secondLastTransition.getTarget())
                            ? new QBETransition(tr.getSource(), stateWithDummy, tr.getAction(), tr.getActionResult())
                            : tr
                    ).collect(toList())).collect(toList());

            for (int testsuiteIndex = 0; testsuiteIndex < testsuite.size(); ++testsuiteIndex) {
                final List<QBETransition> testcase = testsuite.get(testsuiteIndex);
                for (int testcaseIndex = 0; testcaseIndex < testcase.size(); ++testcaseIndex) {
                    addTransition(testcase.get(testcaseIndex));
                    final boolean nonDeterministic = isDeterministic();
                    if (nonDeterministic) {
                        testsuite.remove(testcase);
                        final List<QBETransition> testcaseCopy
                                = new ArrayList<>(testcase.subList(0, testcaseIndex + 1));
                        testsuite = passiveLearn(testsuite, testcaseCopy);
                        if (testcaseCopy.size() > 1 && testcaseIndex + 1 < testcase.size()) {
                            testcaseCopy.addAll(testcase.subList(testcaseIndex + 1, testcase.size()));
                            testsuite.add(testcaseCopy);
                        }
                    }
                }
            }
        }

        removeUnreachableStates();
        deterministic = checkIsDeterministic();
        assert deterministic : "The ELTS should be deterministic after applying passiveLearn, but it's not.";
        return testsuite;
    }
}
