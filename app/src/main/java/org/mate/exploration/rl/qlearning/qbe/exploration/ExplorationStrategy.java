package org.mate.exploration.rl.qlearning.qbe.exploration;

import org.mate.interaction.action.ui.UIAction;
import org.mate.model.fsm.qbe.QBEState;

import java.util.Optional;

/**
 * Defines the interface for an exploration strategy in the context of QBE.
 */
@FunctionalInterface
public interface ExplorationStrategy {

    /**
     * Chooses the next action based on the current state.
     *
     * @param currentState The current state.
     * @return Returns the action that should be applied next if there is such an action.
     */
    Optional<UIAction> chooseAction(QBEState currentState);
}
