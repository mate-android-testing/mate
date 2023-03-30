package org.mate.exploration.rl.qlearning.qbe.exploration;

import org.mate.interaction.action.ui.UIAction;
import org.mate.model.fsm.qbe.QBEState;
import org.mate.utils.Randomness;

import java.util.List;
import java.util.Optional;

/**
 * Provides a random exploration strategy for QBE.
 */
public final class RandomExploration implements ExplorationStrategy {

    /**
     * Chooses a random action from the current state.
     *
     * @param currentState The current state.
     * @return Returns a random action applicable on the current state if possible.
     */
    @Override
    public Optional<UIAction> chooseAction(final QBEState currentState) {
        final List<UIAction> possibleActions = currentState.getActions();
        return possibleActions.isEmpty()
                ? Optional.empty() : Optional.of(Randomness.randomElement(possibleActions));
    }
}
