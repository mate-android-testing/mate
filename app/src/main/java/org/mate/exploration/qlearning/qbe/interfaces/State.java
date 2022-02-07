package org.mate.exploration.qlearning.qbe.interfaces;

import java.util.Set;

/**
 * Defines the interface for a state in the context of QBE.
 *
 * @param <A> The generic action type.
 */
public interface State<A extends Action> {

    /**
     * Retrieves the actions associated with the given state.
     *
     * @return Returns the available actions linked to the given state.
     */
    Set<A> getActions();
}
