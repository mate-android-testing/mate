package org.mate.exploration.qlearning.qbe.algorithms;

import static java.util.stream.Collectors.toList;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.exploration.qlearning.qbe.abstractions.app.Application;
import org.mate.exploration.qlearning.qbe.abstractions.state.State;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.transition_system.TransitionRelation;
import org.mate.exploration.qlearning.qbe.transition_system.TransitionSystem;
import org.mate.exploration.qlearning.qbe.transition_system.TransitionSystemSerializer;
import org.mate.interaction.action.ActionResult;
import org.mate.utils.Pair;
import org.mate.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A QBE testing algorithms that chooses its actions according to the given
 * {@link ExplorationStrategy} and records and serializes a {@link TransitionSystem}.
 */
public final class ApplicationTester<S extends State<A>, A extends Action> extends AbstractTester<S, A> {
    private List<List<TransitionRelation<S, A>>> testsuite = new ArrayList<>();
    private final TransitionSystem<S, A> transitionSystem;

    public ApplicationTester(final Application<S, A> app,
                             final ExplorationStrategy<S, A> explorationStrategy,
                             final int maximumNumberOfActionPerTestCase) {
        super(app, explorationStrategy, maximumNumberOfActionPerTestCase);

        app.reset(); // Ensure that app.getCurrentState() is the initial state of the app.
        this.transitionSystem = new TransitionSystem<>(app.getCurrentState());
    }

    public Set<List<TransitionRelation<S, A>>> getTestsuite() {
        return new HashSet<>(testsuite);
    }

    public TransitionSystem<S, A> getTransitionSystem() {
        return transitionSystem;
    }

    @Override
    public void run() {
        try {
            test();
        } finally {
           if(Properties.QBE_RECORD_TRANSITION_SYSTEM()) {
               final TransitionSystemSerializer<A, S> serializer = new TransitionSystemSerializer<>();
               serializer.serialize(transitionSystem);
               Registry.getEnvironmentManager().fetchTransitionSystem();
           }
        }
    }

    /*
     * Execute the actions chosen by the exploration strategy and update the transition system
     *  accordingly.
     */
    private void test() {
        while (true) {
            app.reset();
            S currentState = app.getCurrentState();
            final List<TransitionRelation<S, A>> testcase = new ArrayList<>();

            boolean noTerminalState;
            boolean noCrash = true;
            boolean nonDeterministic = false;

            do {
                final Optional<A> chosenAction = explorationStrategy.chooseAction(currentState);
                MATE.log_debug("Choose action:" + chosenAction);
                noTerminalState = chosenAction.isPresent();
                if (noTerminalState) {
                    final Pair<Optional<S>, ActionResult> result = app.executeAction(chosenAction.get());
                    final Optional<S> nextState = result.first;
                    MATE.log_debug("Executed action:" + chosenAction.get());

                    /*
                     * From the next line onwards the transition system could be inconsistent.
                     * We need to make sure the algorithms checks for non-determinism and applies
                     * the passiveLearn algorithm if needed. This process must not be interrupted.
                     */
                    Utils.throwOnInterrupt();
                    final TransitionRelation<S, A> transition = new TransitionRelation<>(
                            currentState, chosenAction.get(), nextState.orElse(null), result.second);
                    nonDeterministic = transitionSystem.addTransition(transition);
                    testcase.add(transition);

                    noCrash = nextState.isPresent();
                    if (noCrash) {
                        transitionSystem.addActions(nextState.get().getActions());
                        currentState = nextState.get();
                    }

                    if (nonDeterministic) {
                        MATE.log_debug("Executing passive learn");
                        testsuite = passiveLearn(transitionSystem, testsuite, testcase);

                        // At this point can we allow the algorithm to be interrupted again.

                        transitionSystem.removeUnreachableStates();
                        if (!transitionSystem.isDeterministic()) {
                            throw new AssertionError(
                                    "The transition system should be deterministic after applying passiveLearn");
                        }

                        Utils.throwOnInterrupt();
                    }
                }
            } while (noTerminalState && noCrash && testcase.size() < maximumNumberOfActionPerTestCase);

            testsuite.add(testcase);
        }
    }

    /*
     * If the app is deterministic, then the derived transition system should be deterministic as
     * well. A non deterministic transition system can occur because of a false inference and
     * because of the fuzzy state equivalence definition. This algorithms corrects a
     * non-deterministic transition system, by turning it back into a deterministic one.
     *
     * WARNING: Passive Learn can recuse infinitely, the AUT is non deterministic.
     */
    private List<List<TransitionRelation<S, A>>> passiveLearn(
            final TransitionSystem<S, A> ts,
            List<List<TransitionRelation<S, A>>> testsuite,
            final List<TransitionRelation<S, A>> nonDeterministicTestcase) {

        final int testcaseLength = nonDeterministicTestcase.size();
        if (testcaseLength == 1) {
            throw new AssertionError(
                    "The first action should always be deterministic," +
                            " if the AUT is deterministic.");

        } else {
            final TransitionRelation<S, A> conflictingTransition
                    = nonDeterministicTestcase.remove(testcaseLength - 1);
            final TransitionRelation<S, A> secondLastTransition
                    = nonDeterministicTestcase.get(testcaseLength - 2);
            final S stateWithDummy = app.copyWithDummyComponent(secondLastTransition.to);
            ts.removeTransition(conflictingTransition);
            ts.removeTransition(secondLastTransition);
            ts.addTransition(new TransitionRelation<>(secondLastTransition.from,
                    secondLastTransition.trigger,
                    stateWithDummy,
                    secondLastTransition.actionResult));

            testsuite.add(nonDeterministicTestcase);

            testsuite = testsuite.stream().map(testcase -> testcase.stream()
                    .map(tr -> tr.from.equals(secondLastTransition.from)
                            && Objects.equals(tr.to, secondLastTransition.to)
                            ? new TransitionRelation<>(tr.from, tr.trigger, stateWithDummy, tr.actionResult)
                            : tr
                    ).collect(toList())).collect(toList());

            for (int testsuiteIndex = 0; testsuiteIndex < testsuite.size(); ++testsuiteIndex) {
                final List<TransitionRelation<S, A>> testcase = testsuite.get(testsuiteIndex);
                for (int testcaseIndex = 0; testcaseIndex < testcase.size(); ++testcaseIndex) {
                    final boolean nonDeterministic = ts.addTransition(testcase.get(testcaseIndex));
                    if (nonDeterministic) {
                        testsuite.remove(testcase);
                        final List<TransitionRelation<S, A>> testcaseCopy
                                = testcase.subList(0, testcaseIndex + 1)
                                .stream()
                                .map(TransitionRelation::new)
                                .collect(toList());
                        testsuite = passiveLearn(ts, testsuite, testcaseCopy);
                        if (testcaseCopy.size() > 1 && testcaseIndex + 1 < testcase.size()) {
                            testcase.subList(testcaseIndex + 1, testcase.size())
                                    .stream()
                                    .map(TransitionRelation::new)
                                    .forEach(testcaseCopy::add);
                            testsuite.add(testcaseCopy);
                        }
                    }
                }
            }
        }
        return testsuite;
    }
}

