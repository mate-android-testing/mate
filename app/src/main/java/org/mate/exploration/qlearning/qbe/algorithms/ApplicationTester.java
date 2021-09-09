package org.mate.exploration.qlearning.qbe.algorithms;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.Application;
import org.mate.exploration.qlearning.qbe.interfaces.State;
import org.mate.exploration.qlearning.qbe.transitionSystem.TransitionRelation;
import org.mate.exploration.qlearning.qbe.transitionSystem.TransitionSystem;
import org.mate.utils.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.O)
public final class ApplicationTester<S extends State<A>, A extends Action> {

  private final Application<S, A> app;
  private final ExplorationStrategy<S, A> explorationStrategy;
  private final Duration timeout;
  private final int maximumNumberOfActionPerTestcase;

  public ApplicationTester(final Application<S, A> app,
                           final ExplorationStrategy<S, A> explorationStrategy,
                           final Duration timeout, final int maximumNumberOfActionPerTestcase) {
    this.app = Objects.requireNonNull(app);
    this.explorationStrategy = Objects.requireNonNull(explorationStrategy);
    this.timeout = Objects.requireNonNull(timeout);
    this.maximumNumberOfActionPerTestcase = maximumNumberOfActionPerTestcase;

    if (timeout.isZero() || timeout.isNegative()) {
      throw new IllegalArgumentException("Timeout needs to be a positive duration.");
    }

    if (maximumNumberOfActionPerTestcase <= 0) {
      throw new IllegalArgumentException(
              "The maximum number of actions per test case need to be at least 1.");
    }
  }

  public Pair<TransitionSystem<S, A>, Set<List<TransitionRelation<S, A>>>> exploreApplication() {
    final Set<List<TransitionRelation<S, A>>> testsuite = new HashSet<>();
    final TransitionSystem<S, A> transitionSystem = new TransitionSystem<>(app.getInitialState());

    final Instant startTime = Instant.now();
    while (noTimeout(startTime)) {
      app.reset();
      S currentState = app.getInitialState();
      final List<TransitionRelation<S, A>> testcase = new ArrayList<>();

      boolean noCrash = true; // Initialization is not necessary, but the compiler cannot figure it out.
      boolean noTerminalState;
      do {
        final Optional<A> chosenAction = explorationStrategy.chooseAction(currentState);
        noTerminalState = chosenAction.isPresent();
        if (noTerminalState) {
          final Optional<S> nextState = app.executeAction(chosenAction.get());
          final TransitionRelation<S, A> transition = new TransitionRelation<>(currentState,
                  chosenAction.get(), nextState.orElse(null));
          final boolean nonDeterministic = transitionSystem.addTransition(transition);
          testcase.add(transition);

          noCrash = nextState.isPresent();
          if (noCrash) {
            // Not sure if updating the actions set is necessary but the paper does it.
            transitionSystem.addActions(nextState.get().getActions());
            if (nonDeterministic) {
              final List<TransitionRelation<S, A>> testcaseCopy = testcase.stream()
                      .map(TransitionRelation::new).collect(Collectors.toList());
              passiveLearn(transitionSystem, testsuite, testcaseCopy);
            }
            currentState = nextState.get();
          }
        }
      } while (noTerminalState && noCrash && noTimeout(startTime)
              && testcase.size() < maximumNumberOfActionPerTestcase);

      testsuite.add(testcase);
    }

    transitionSystem.removeUnreachableStates();
    assert transitionSystem.isDeterministic();
    return new Pair<>(transitionSystem, testsuite);
  }

  private void passiveLearn(
          final TransitionSystem<S, A> ts,
          final Set<List<TransitionRelation<S, A>>> testsuite,
          final List<TransitionRelation<S, A>> nonDeterministicTestcase) {

    final int testcaseLength = nonDeterministicTestcase.size();
    if (testcaseLength < 2) {
      throw new AssertionError("Passive Learn assumes that the test case length is at least 2.");
    }

    testsuite.add(nonDeterministicTestcase);
    final TransitionRelation<S, A> conflictingTransition = nonDeterministicTestcase.get(
            testcaseLength - 1);
    final TransitionRelation<S, A> secondLastTransition = nonDeterministicTestcase.get(
            testcaseLength - 2);
    final S stateWithDummy = app.copyWithDummyComponent(conflictingTransition.from);
    ts.removeTransition(conflictingTransition);
    ts.removeTransition(secondLastTransition);
    ts.addTransition(
            new TransitionRelation<>(secondLastTransition.from, secondLastTransition.trigger,
                    stateWithDummy));

    testsuite.forEach(testcase ->
            testcase.replaceAll(tr -> {
              if (tr.from.equals(secondLastTransition.from) && tr.to.equals(secondLastTransition.to)) {
                return new TransitionRelation<>(tr.from, tr.trigger, stateWithDummy);
              } else {
                return tr;
              }
            }));

    for (final List<TransitionRelation<S, A>> testcase : testsuite) {
      for (int i = 0; i < testcase.size(); ++i) {
        final boolean nonDeterministic = ts.addTransition(testcase.get(i));
        if (nonDeterministic) {
          passiveLearn(ts, testsuite, testcase.subList(0, i + 1));
          return;
        }
      }
    }

    ts.removeUnreachableStates();
    assert ts.isDeterministic() : "The transition system should be deterministic after applying passiveLearn";
  }

  private boolean noTimeout(final Instant startTime) {
    return timeout.compareTo(Duration.between(startTime, Instant.now())) > 0;
  }
}

