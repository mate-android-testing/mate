package org.mate.exploration.rl.qlearning.qbe.exploration;

import static org.mate.utils.Randomness.getRandomlyDistributedKey;
import static org.mate.utils.StreamUtils.distinctByKey;

import org.mate.exploration.rl.qlearning.qbe.qmatrix.QMatrix;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.fsm.qbe.QBEState;
import org.mate.utils.Randomness;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides a simple Q-Learning based exploration strategy as outlined in Algorithm 3.
 */
public final class QMMatrixBasedExploration implements ExplorationStrategy {

    /**
     * The transition prioritization matrix or shortly denoted as q-matrix.
     */
    private final QMatrix qmatrix;

    /**
     * The set of abstract actions.
     */
    private final QMatrix.AbstractActions abstractActions;

    /**
     * Initialises the Q-Learning based exploration strategy with the given q-matrix.
     *
     * @param qmatrix The given q-matrix.
     */
    public QMMatrixBasedExploration(final QMatrix qmatrix) {
        this.qmatrix = Objects.requireNonNull(qmatrix);
        this.abstractActions = qmatrix.getActionLabelingFunction();
    }

    /**
     * Chooses the next action, see Algorithm 3 for more details.
     *
     * @param currentState The current state.
     * @return Returns the action that should be applied next.
     */
    @Override
    public Optional<UIAction> chooseAction(final QBEState currentState) {

        final List<UIAction> currentActions = currentState.getActions();

        if (currentActions.isEmpty()) {
            return Optional.empty();
        } else {
            final Map<Integer, Double> qMap = currentActions.stream()
                    .filter(distinctByKey(abstractActions::getAbstractActionIndex))
                    .collect(
                            Collectors.toMap(abstractActions::getAbstractActionIndex,
                                    action -> qmatrix.getValue(currentState, action)));

            final int chosenAbstractActionIndex = getRandomlyDistributedKey(qMap);
            final List<UIAction> possibleActions = currentState.getActions().stream()
                    .filter(a -> abstractActions.getAbstractActionIndex(a) == chosenAbstractActionIndex)
                    .collect(Collectors.toList());
            return Optional.of(Randomness.randomElement(possibleActions));
        }
    }
}
