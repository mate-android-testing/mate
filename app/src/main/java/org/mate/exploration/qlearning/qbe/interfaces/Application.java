package org.mate.exploration.qlearning.qbe.interfaces;

import org.mate.utils.Pair;

import java.util.Optional;

import static org.mate.interaction.UIAbstractionLayer.ActionResult;

/**
 * Defines the interface for an application in the context of QBE.
 *
 * @param <S> The generic state type.
 * @param <A> The generic action type.
 */
public interface Application<S extends State<A>, A extends Action> {

    /**
     * Retrieves the current state in which the application is right now.
     *
     * @return Returns the current state.
     */
    S getCurrentState();

    /**
     * Executes the given action.
     *
     * @param action The action to be executed.
     * @return Returns a pair containing the new state and the action result.
     */
    Pair<Optional<S>, ActionResult> executeAction(A action);

    /**
     * Resets the application state.
     */
    void reset();

    S copyWithDummyComponent(S conflictingState);
}
