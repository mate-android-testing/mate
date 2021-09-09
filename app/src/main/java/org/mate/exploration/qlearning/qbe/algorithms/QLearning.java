package org.mate.exploration.qlearning.qbe.algorithms;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.exploration.implementations.QMatrix;
import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.State;
import org.mate.exploration.qlearning.qbe.transitionSystem.TransitionRelation;
import org.mate.exploration.qlearning.qbe.transitionSystem.TransitionSystem;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class QLearning<S extends State<A>, A extends Action> {

  private final Objective<S, A> objective;
  private final QMatrix.AbstractStates<S, A> abstractStates;
  private final QMatrix.AbstractActions<A> abstractActions;
  private final double epsilonUpdateFactor;
  private final int maximumNumberOfActions;
  private final double discountFactor;

  public QLearning(final Objective<S, A> objective,
                   final QMatrix.AbstractStates<S, A> abstractStates,
                   final QMatrix.AbstractActions<A> abstractActions, final double epsilonUpdateFactor,
                   final int maximumNumberOfActions, final double discountFactor) {
    this.objective = Objects.requireNonNull(objective);
    this.abstractStates = Objects.requireNonNull(abstractStates);
    this.abstractActions = Objects.requireNonNull(abstractActions);
    this.epsilonUpdateFactor = epsilonUpdateFactor;
    this.maximumNumberOfActions = maximumNumberOfActions;
    this.discountFactor = discountFactor;

    if (epsilonUpdateFactor < 0 || epsilonUpdateFactor > 1) {
      throw new IllegalArgumentException(
              "The epsilon update factor needs to be between 0 and 1 (both inclusive).");
    }

    if (maximumNumberOfActions <= 0) {
      throw new IllegalArgumentException("The maximum number of actions needs to be positive.");
    }

    if (discountFactor < 0 || discountFactor > 1) {
      throw new IllegalArgumentException(
              "The discount factor needs to be between 0 and 1 (both inclusive).");
    }

  }

  public QMatrix<S, A> qLearning(final Set<TransitionSystem<S, A>> transitionSystems) {
    final QMatrix<S, A> qMatrix = new QMatrix<>(abstractStates, abstractActions);
    final QMatrix<S, A> historyMatrix = new QMatrix<>(abstractStates, abstractActions);
    final List<TransitionSystem<S, A>> transitionSystemList = new ArrayList<>(transitionSystems);
    Collections.shuffle(transitionSystemList);

    double epsilon = 1.0;
    for (final TransitionSystem<S, A> transitionSystem : transitionSystemList) {
      S currentState = transitionSystem.getInitialState();
      Optional<A> currentAction = chooseNextAction(transitionSystem, currentState, qMatrix,
              epsilon);
      int numberOfActions = 0;
      while (currentState != null && currentAction.isPresent()
              && numberOfActions < maximumNumberOfActions) {
        final Set<TransitionRelation<S, A>> nextTransitions = transitionSystem.nextStates(
                currentState, currentAction.get());
        if (nextTransitions.size() > 1) {
          throw new AssertionError("The transition models are expected to be deterministic.");
        } else {
          final S nextState = nextTransitions.stream().findFirst().get().to;
          final Optional<A> nextAction =
                  nextState != null ? chooseNextAction(transitionSystem, nextState, qMatrix,
                          epsilon) : Optional.empty();
          if (nextAction.isPresent()) {
            /*
             * The algorithm in the paper simply assumes that nextState is not a crash.
             * Moreover, it assumes that nextState is not a terminal state, so there is a next action.
             * In general both assumptions might be invalid in which case the schema for updating the qMatrix fails.
             * So, the updating the qMatrix is guarded by this if.
             *
             * However, the history matrix could still be updated regardless.
             * Because this case is not accurately described in the paper (and there is no public reference implementation (anymore)) a choice has to made whether to update the history matrix if the qMatrix cannot be updated.
             * It seems reasonable to assume that none of the two matrices should be updated independently of another, so this choice is taken.
             */
            historyMatrix.updateValue(currentState, currentAction.get(), d -> d + 1);
            final double oldQValue = qMatrix.getValue(currentState, currentAction.get());
            final int objectiveValue =
                    objective.test(currentState, currentAction.get()) ? 1 : 0;
            final double newQValue =
                    (objectiveValue
                            + discountFactor * qMatrix.getValue(nextState, nextAction.get())
                            - oldQValue
                    ) / historyMatrix.getValue(currentState, currentAction.get())
                            + oldQValue;
            qMatrix.setValue(currentState, currentAction.get(), newQValue);
            qMatrix.normalizeActions(currentState);
          }
          currentState = nextState;
          currentAction = nextAction;
          ++numberOfActions;
        }
      }
      epsilon *= epsilonUpdateFactor;
    }

    return qMatrix;
  }

  private Optional<A> chooseNextAction(final TransitionSystem<S, A> transitionSystem,
                                       final S currentState, final QMatrix<S, A> qMatrix, final double epsilon) {
    final Set<A> candidateActions = transitionSystem.nextActions(currentState);
    if (candidateActions.isEmpty()) {
      return Optional.empty();
    } else if (Randomness.getRnd().nextDouble() < epsilon) {
      return Optional.of(
              candidateActions.stream().skip(Randomness.getRnd().nextInt(candidateActions.size())).findFirst().get());
    } else {
      return Optional.of(candidateActions.stream()
              .max(Comparator.comparingDouble(a -> qMatrix.getValue(currentState, a))).get());
    }
  }


  @FunctionalInterface
  public interface Objective<S extends State<A>, A extends Action> extends BiPredicate<S, A> {

  }

}
