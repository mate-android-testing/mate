package org.mate.exploration.qlearning.qbe.abstractions.state;

import org.mate.exploration.qlearning.qbe.abstractions.action.Action;

import java.util.Map;
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

    /**
     * Retrieves the feature map.
     *
     * @return Returns the feature map.
     */
    Map<String, Integer> getFeatureMap();
}
