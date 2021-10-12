package org.mate.exploration.qlearning.qbe.algorithms;

import static org.mate.interaction.UIAbstractionLayer.ActionResult;
import static java.util.stream.Collectors.toList;

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
import java.util.stream.IntStream;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class ApplicationTester<S extends State<A>, A extends Action> implements Algorithm {

  private final Application<S, A> app;
  private final ExplorationStrategy<S, A> explorationStrategy;
  private final long timeoutInMilliseconds;
  private final int maximumNumberOfActionPerTestcase;

  private Set<List<TransitionRelation<S, A>>> testsuite = new HashSet<>();
  private final TransitionSystem<S, A> transitionSystem;

  public ApplicationTester(final Application<S, A> app,
                           final ExplorationStrategy<S, A> explorationStrategy,
                           final long timeoutInMilliseconds, final int maximumNumberOfActionPerTestcase) {
    this.app = Objects.requireNonNull(app);
    this.explorationStrategy = Objects.requireNonNull(explorationStrategy);
    this.timeoutInMilliseconds = timeoutInMilliseconds;
    this.maximumNumberOfActionPerTestcase = maximumNumberOfActionPerTestcase;

    app.reset();
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
      boolean nonDeterministic = false;
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
          nonDeterministic = transitionSystem.addTransition(transition);
          testcase.add(transition);

          noCrash = nextState.isPresent();
          if (noCrash) {
            transitionSystem.addActions(nextState.get().getActions());
            if (nonDeterministic) {
              MATE.log_debug("Executing passive learn");
              testsuite = new HashSet<>(passiveLearn(transitionSystem, new ArrayList<>(testsuite), testcase));
              transitionSystem.removeUnreachableStates();
              if (!transitionSystem.isDeterministic()) {
                throw new AssertionError("The transition system should be deterministic after applying passiveLearn");
              }
            }
            currentState = nextState.get();
          }
        }
      } while (noTerminalState && noCrash && !nonDeterministic && testcase.size() < maximumNumberOfActionPerTestcase && noTimeout(startTime));
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
      /*
       * The algorithm as described in the original QBE paper simply assumes 'testcaseLength >= 2'.
       * This assumption does not hold in general. Moreover, there seems to be no good solution to
       * edge-case. Removing the test from the testsuite and the transition from the
       * TransitionSystem basically cases the transition to be ignored and at least keeps the
       * transition system deterministic.
       */
      testsuite = testsuite.stream().filter(testcase ->
              !IntStream.range(0, nonDeterministicTestcase.size()).allMatch(i -> nonDeterministicTestcase.get(i).equals(testcase.get(i)))
      ).collect(toList());
      if (testsuite.contains(nonDeterministicTestcase)) throw new AssertionError();
      final TransitionRelation<S, A> tr = nonDeterministicTestcase.remove(0);
      ts.removeTransition(tr);
    } else {
      /*
       * Added the the nonDeterministicTestcase to the testsuite should be done according to the
       * algorithm as described in the QBE paper. However, doing so causes an infinite recursion.
       * Not adding the testcase does not seem to hurt while preventing the infinite recursion.
       * Therefore the next line should be kept commented out.
       *
       * TODO: Investigate the exact cause of the infinite recursion and potential other fixes.
       */
      testsuite.add(nonDeterministicTestcase);
      final TransitionRelation<S, A> conflictingTransition = nonDeterministicTestcase.get(
              testcaseLength - 1);
      final TransitionRelation<S, A> secondLastTransition = nonDeterministicTestcase.get(
              testcaseLength - 2);
      final S stateWithDummy = app.copyWithDummyComponent(conflictingTransition.from);
      if (stateWithDummy.equals(conflictingTransition.from)) throw new AssertionError();
      ts.removeTransition(conflictingTransition);
      ts.removeTransition(secondLastTransition);
      ts.addTransition(new TransitionRelation<>(secondLastTransition.from, secondLastTransition.trigger, stateWithDummy, secondLastTransition.actionResult));

      testsuite = testsuite.stream().map(testcase -> testcase.stream().map(tr ->
              tr.from.equals(secondLastTransition.from) && Objects.equals(tr.to, secondLastTransition.to)
                      ? new TransitionRelation<>(tr.from, tr.trigger, stateWithDummy, tr.actionResult)
                      : tr
      ).collect(toList())).collect(toList());

      /*
      boolean nonDeterministic = false;
      do {
        for (final List<TransitionRelation<S, A>> testcase : testsuite) {
          for (int i = 0; i < testcase.size(); ++i) {
            nonDeterministic = ts.addTransition(testcase.get(i)) || nonDeterministic;
            if (nonDeterministic) {
              final List<TransitionRelation<S, A>> testcaseCopy = testcase.subList(0, i + 1).stream().map(TransitionRelation::new).collect(toList());
              testsuite = passiveLearn(ts, testsuite, testcaseCopy);
            }
          }
          if (nonDeterministic) break;
        }
      } while (nonDeterministic);
       */

      boolean nonDeterministic = false;
      do {
        int testsuiteIndex = 0;
        int testcaseIndex = 0;
        for (; testsuiteIndex < testsuite.size() && !nonDeterministic; ++testsuiteIndex) {
          final List<TransitionRelation<S, A>> testcase = testsuite.get(testsuiteIndex);
          for (; testcaseIndex < testcase.size() && !nonDeterministic; ++testcaseIndex) {
            nonDeterministic = ts.addTransition(testcase.get(testcaseIndex));
          }
        }

        if (nonDeterministic) {
          final List<TransitionRelation<S, A>> testcaseCopy = testsuite.get(testsuiteIndex).subList(0, testcaseIndex + 1).stream().map(TransitionRelation::new).collect(toList());
          testsuite = passiveLearn(ts, testsuite, testcaseCopy);
        }
      } while (nonDeterministic);

    }
    return testsuite;
  }

  private boolean noTimeout(final long startTime) {
    final long currentTime = System.currentTimeMillis();
    return currentTime - startTime < timeoutInMilliseconds;
  }

}

