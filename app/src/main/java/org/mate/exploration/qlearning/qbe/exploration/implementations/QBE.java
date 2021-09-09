package org.mate.exploration.qlearning.qbe.exploration.implementations;


import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.State;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mate.exploration.qlearning.qbe.util.DistributionRandomNumberGenerator.getDistributedRandomNumber;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class QBE<S extends State<A>, A extends Action> implements ExplorationStrategy<S, A> {

  private final QMatrix<S, A> qmatrix;
  private final QMatrix.AbstractActions<A> abstractActions;
  private final Random random = new Random();


  public QBE(final QMatrix<S, A> qmatrix) {
    this.qmatrix = Objects.requireNonNull(qmatrix);
    this.abstractActions = qmatrix.getActionLabelingFunction();
  }

  @Override
  public Optional<A> chooseAction(final S currentState) {
    final Set<A> currentActions = currentState.getActions();
    if (currentActions.isEmpty()) {
      return Optional.empty();
    } else {
      final Map<Integer, Double> qMap = currentActions.stream().collect(
              Collectors.toMap(abstractActions::getAbstractActionIndex,
                      action -> qmatrix.getValue(currentState, action)));
      final int chosenAbstractActionIndex = getDistributedRandomNumber(qMap);
      final Set<A> possibleActions = currentState.getActions().stream()
              .filter(a -> abstractActions.getAbstractActionIndex(a) == chosenAbstractActionIndex)
              .collect(Collectors.toSet());
      return possibleActions.stream().skip(random.nextInt(possibleActions.size())).findFirst();
    }
  }
}
