package org.mate.exploration.qlearning.qbe.exploration.implementations;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.State;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class RandomExploration<S extends State<A>, A extends Action> implements
        ExplorationStrategy<S, A> {

  private final Random random = new Random();

  @Override
  public Optional<A> chooseAction(final S currentState) {
    final Set<A> possibleActions = currentState.getActions();
    if (possibleActions.isEmpty()) {
      return Optional.empty();
    } else {
      return possibleActions.stream().skip(random.nextInt(possibleActions.size())).findFirst();
    }
  }
}
