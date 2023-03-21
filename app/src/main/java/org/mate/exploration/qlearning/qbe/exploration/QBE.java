package org.mate.exploration.qlearning.qbe.exploration;


import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.exploration.qlearning.qbe.abstractions.state.State;
import org.mate.exploration.qlearning.qbe.qmatrix.QMatrix;
import org.mate.utils.Randomness;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mate.utils.Randomness.getRandomlyDistributedKey;
import static org.mate.utils.StreamUtils.distinctByKey;

/**
 * Provides a simple Q-Learning based exploration strategy as outlined in Algorithm 3.
 *
 * @param <S> The generic state type.
 * @param <A> The generic action type.
 */
public final class QBE<S extends State<A>, A extends Action> implements ExplorationStrategy<S, A> {

    /**
     * The transition prioritization matrix or shortly denoted as q-matrix.
     */
    private final QMatrix<S, A> qmatrix;

    /**
     * The set of abstract actions.
     */
    private final QMatrix.AbstractActions<A> abstractActions;

    /**
     * Initialises the Q-Learning based exploration strategy with the given q-matrix.
     *
     * @param qmatrix The given q-matrix.
     */
    public QBE(final QMatrix<S, A> qmatrix) {
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
            final int chosenAbstractActionIndex = getRandomlyDistributedKey(qMap);
            final Set<A> possibleActions = currentState.getActions().stream()
                    .filter(a -> abstractActions.getAbstractActionIndex(a) == chosenAbstractActionIndex)
                    .collect(Collectors.toSet());
            return Optional.of(Randomness.randomElement(possibleActions));
        }
    }
}
