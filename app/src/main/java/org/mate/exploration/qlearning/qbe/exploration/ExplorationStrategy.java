package org.mate.exploration.qlearning.qbe.exploration;

import org.mate.exploration.qlearning.qbe.abstractions.action.Action;
import org.mate.exploration.qlearning.qbe.abstractions.state.State;

import java.util.Optional;

/**
 * Defines the interface for an exploration strategy in the context of QBE.
 *
 * @param <S> The generic state type.
 * @param <A> The generic action type.
 */
@FunctionalInterface
public interface ExplorationStrategy<S extends State<A>, A extends Action> {

    /**
     * Chooses the next action based on the current state.
     *
     * @param currentState The current state.
     * @return Returns the action that should be applied next if there is such an action.
     */
    Optional<A> chooseAction(S currentState);
}
