package org.mate.exploration.qlearning.qbe.algorithms;

import static org.mate.interaction.UIAbstractionLayer.ActionResult;

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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class ApplicationTester<S extends State<A>, A extends Action> implements Algorithm {

  private final Application<S, A> app;
  private final ExplorationStrategy<S, A> explorationStrategy;
  private final long timeoutInMilliseconds;
  private final int maximumNumberOfActionPerTestcase;

  private final Set<List<TransitionRelation<S, A>>> testsuite = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final TransitionSystem<S, A> transitionSystem;

  public ApplicationTester(final Application<S, A> app,
                           final ExplorationStrategy<S, A> explorationStrategy,
                           final long timeoutInMilliseconds, final int maximumNumberOfActionPerTestcase) {
    this.app = Objects.requireNonNull(app);
    this.explorationStrategy = Objects.requireNonNull(explorationStrategy);
    this.timeoutInMilliseconds = timeoutInMilliseconds;
    this.maximumNumberOfActionPerTestcase = maximumNumberOfActionPerTestcase;

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
    return testsuite;
  }

  public TransitionSystem<S, A> getTransitionSystem() {
    return transitionSystem;
  }

  public void run() {
    final long startTime = System.currentTimeMillis();
    while (noTimeout(startTime)) {
      app.reset();
      S currentState = app.getCurrentState();
      List<TransitionRelation<S, A>> testcase = new ArrayList<>();

      boolean noCrash = true; // Initialization is not necessary, but the compiler cannot figure it out.
      boolean noTerminalState;
      do {
        final Optional<A> chosenAction = explorationStrategy.chooseAction(currentState);
        MATE.log_debug("Choose action:" + chosenAction.toString());
        noTerminalState = chosenAction.isPresent();
        if (noTerminalState) {
          final Pair<Optional<S>, ActionResult> result = app.executeAction(chosenAction.get());
          final Optional<S> nextState = result.first;
          MATE.log_debug("Executed action action:" + chosenAction.get().toString());
          final TransitionRelation<S, A> transition = new TransitionRelation<>(currentState,
                  chosenAction.get(), nextState.orElse(null), result.second);
          final boolean nonDeterministic = transitionSystem.addTransition(transition);
          testcase.add(transition);

          noCrash = nextState.isPresent();
          if (noCrash) {
            transitionSystem.addActions(nextState.get().getActions());
            if (nonDeterministic) {
              final List<TransitionRelation<S, A>> testcaseCopy = testcase.stream()
                      .map(TransitionRelation::new).collect(Collectors.toList());
              testcase = passiveLearn(transitionSystem, testsuite, testcaseCopy);
            }
            currentState = nextState.get();
          }
        }
      } while (noTerminalState && noCrash && testcase.size() < maximumNumberOfActionPerTestcase && noTimeout(startTime));

      testsuite.add(testcase);
    }
  }

  private List<TransitionRelation<S, A>> passiveLearn(
          final TransitionSystem<S, A> ts,
          final Set<List<TransitionRelation<S, A>>> testsuite,
          final List<TransitionRelation<S, A>> nonDeterministicTestcase) {

    final int testcaseLength = nonDeterministicTestcase.size();
    if (testcaseLength < 2) {
      /*
       * Passive learn simply assumes that the test case has a length of at least two.
       * For very small apps (e.g. com.zola.bmi.akp) this assumption does not hold.
       * TODO: Check whether this assumptions holds for bigger apps.
       */
      TransitionRelation<S, A> relation = nonDeterministicTestcase.remove(0);
      MATE.log_warn("Passive learn: Found singe-transition testcase: " + relation);
      boolean deterministic;
      int iteration = 1;
      do {
        MATE.log_debug("Passive Learn: Edge case: Attempt " + iteration);
        ts.removeTransition(relation); // Ensure the transition system remains
        final S dummy = app.copyWithDummyComponent(relation.to);
        relation = new TransitionRelation<>(relation.from, relation.trigger, dummy, relation.actionResult);
        deterministic = ts.addTransition(relation);
      } while (!deterministic);
      MATE.log_debug("Passive Learn: Edge case: Done on iteration: " + iteration);
      nonDeterministicTestcase.add(relation);
      return nonDeterministicTestcase;
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
            new TransitionRelation<>(secondLastTransition.from, secondLastTransition.trigger, stateWithDummy, secondLastTransition.actionResult));

    testsuite.forEach(testcase ->
            testcase.replaceAll(tr -> {
              if (tr.from.equals(secondLastTransition.from) && Objects.equals(tr.to, secondLastTransition.to)) {
                return new TransitionRelation<>(tr.from, tr.trigger, stateWithDummy, tr.actionResult);
              } else {
                return tr;
              }
            }));

    for (final List<TransitionRelation<S, A>> testcase : testsuite) {
      for (int i = 0; i < testcase.size(); ++i) {
        final boolean nonDeterministic = ts.addTransition(testcase.get(i));
        if (nonDeterministic) {
          final List<TransitionRelation<S, A>> testcaseCopy = testcase.subList(0, i + 1).stream().map(TransitionRelation::new).collect(Collectors.toList());
          passiveLearn(ts, testsuite, testcaseCopy);
          return nonDeterministicTestcase;
        }
      }
    }

    ts.removeUnreachableStates();
    assert ts.isDeterministic() : "The transition system should be deterministic after applying passiveLearn";
    return nonDeterministicTestcase;
  }

  private boolean noTimeout(final long startTime) {
    final long currentTime = System.currentTimeMillis();
    return currentTime - startTime < timeoutInMilliseconds;
  }

}

