package org.mate.exploration.qlearning.qbe.exploration.implementations;

import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.State;
import org.mate.utils.Randomness;

import java.util.Optional;
import java.util.Set;

public final class RandomExploration<S extends State<A>, A extends Action> implements ExplorationStrategy<S, A> {

  @Override
  public Optional<A> chooseAction(final S currentState) {
    final Set<A> possibleActions = currentState.getActions();
    return possibleActions.isEmpty() ? Optional.empty() : Optional.of(Randomness.randomElement(possibleActions));
  }
}
