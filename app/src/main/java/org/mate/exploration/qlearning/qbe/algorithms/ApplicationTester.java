package org.mate.exploration.qlearning.qbe.algorithms;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.MATE;
import org.mate.exploration.Algorithm;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.Application;
import org.mate.exploration.qlearning.qbe.interfaces.State;
import org.mate.exploration.qlearning.qbe.transitionSystem.TransitionRelation;
import org.mate.exploration.qlearning.qbe.transitionSystem.TransitionSystem;
import org.mate.utils.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.mate.interaction.UIAbstractionLayer.ActionResult;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class ApplicationTester<S extends State<A>, A extends Action> implements Algorithm {

    private final Application<S, A> app;
    private final ExplorationStrategy<S, A> explorationStrategy;
    private final long timeoutInMilliseconds;
    private final int maximumNumberOfActionPerTestcase;

    private List<List<TransitionRelation<S, A>>> testsuite = new ArrayList<>();
    private final TransitionSystem<S, A> transitionSystem;

    public ApplicationTester(final Application<S, A> app,
                             final ExplorationStrategy<S, A> explorationStrategy,
                             final long timeoutInMilliseconds,
                             final int maximumNumberOfActionPerTestcase) {
        this.app = Objects.requireNonNull(app);
        this.explorationStrategy = Objects.requireNonNull(explorationStrategy);
        this.timeoutInMilliseconds = timeoutInMilliseconds;
        this.maximumNumberOfActionPerTestcase = maximumNumberOfActionPerTestcase;

        app.reset(); // Ensure that app.getCurrentState() is the initial state of the app.
        this.transitionSystem = new TransitionSystem<>(app.getCurrentState());

        if (timeoutInMilliseconds <= 0) {
            throw new IllegalArgumentException("The timeout must be a positive value");
        }

        if (maximumNumberOfActionPerTestcase <= 0) {
            throw new IllegalArgumentException(
                    "The maximum number of actions per test case need to be at least 1.");
        }

    }

    public Set<List<TransitionRelation<S, A>>> getTestsuite() {
        return new HashSet<>(testsuite);
    }

    public TransitionSystem<S, A> getTransitionSystem() {
        return transitionSystem;
    }

    @Override
    public void run() {

        final long startTime = System.currentTimeMillis();

        while (!reachedTimeout(startTime)) {

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
                        transitionSystem.removeUnreachableStates();
                        if (!transitionSystem.isDeterministic()) {
                            throw new AssertionError(
                                    "The transition system should be deterministic after applying passiveLearn");
                        }
                    }

                }
            } while (noTerminalState && noCrash && !nonDeterministic
                    && testcase.size() < maximumNumberOfActionPerTestcase && !reachedTimeout(startTime));
            if (!nonDeterministic) testsuite.add(testcase);
        }
    }

    private List<List<TransitionRelation<S, A>>> passiveLearn(
            final TransitionSystem<S, A> ts,
            List<List<TransitionRelation<S, A>>> testsuite,
            final List<TransitionRelation<S, A>> nonDeterministicTestcase) {

        final int testcaseLength = nonDeterministicTestcase.size();
        MATE.log_debug("Found non-deterministic testcase of length " + testcaseLength);
        if (testcaseLength == 1) {
            final TransitionRelation<S, A> tr = nonDeterministicTestcase.remove(0);
            ts.removeTransition(tr);
            testsuite = testsuite.stream()
                    .filter(testcase -> testcase.isEmpty() || !tr.equals(testcase.get(0)))
                    .collect(toList());
            if (testsuite.contains(nonDeterministicTestcase)) throw new AssertionError();
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

            outer:
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
                        if (testcaseCopy.size() > 1) {
                            if (testcaseIndex + 1 < testcase.size())
                                testcase.subList(testcaseIndex + 1, testcase.size())
                                        .stream()
                                        .map(TransitionRelation::new)
                                        .forEach(testcaseCopy::add);
                            testsuite.add(testcaseCopy);
                        }
                        break outer;
                    }
                }
            }
        }
        return testsuite;
    }

    /**
     * Checks whether the specified timeout has been reached.
     *
     * @param startTime The starting time of the exploration.
     * @return Returns {@code true} if the timeout has been reached, otherwise {@code false}.
     */
    private boolean reachedTimeout(final long startTime) {
        final long currentTime = System.currentTimeMillis();
        return currentTime - startTime > timeoutInMilliseconds;
    }
}

