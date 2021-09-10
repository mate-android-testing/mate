package org.mate.exploration.qlearning.qbe.exploration.implementations;


import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.State;
import org.mate.exploration.qlearning.qbe.qmatrix.QMatrix;
import org.mate.utils.Randomness;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mate.utils.Randomness.getDistributedRandomNumber;
import static org.mate.utils.StreamUtils.distinctByKey;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class QBE<S extends State<A>, A extends Action> implements ExplorationStrategy<S, A> {

  private final QMatrix<S, A> qmatrix;
  private final QMatrix.AbstractActions<A> abstractActions;


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
      final Map<Integer, Double> qMap = currentActions.stream()
              .filter(distinctByKey(abstractActions::getAbstractActionIndex))
              .collect(
                      Collectors.toMap(abstractActions::getAbstractActionIndex,
                              action -> qmatrix.getValue(currentState, action)));
      final int chosenAbstractActionIndex = getDistributedRandomNumber(qMap);
      final Set<A> possibleActions = currentState.getActions().stream()
              .filter(a -> abstractActions.getAbstractActionIndex(a) == chosenAbstractActionIndex)
              .collect(Collectors.toSet());
      return Optional.of(Randomness.randomElement(possibleActions));
    }
  }
}
