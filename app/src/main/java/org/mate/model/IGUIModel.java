package org.mate.model;

import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;

import java.util.Set;

public interface IGUIModel {

    /**
     * Adds a (new) transition to the GUI model.
     *
     * @param source The source state.
     * @param target The target state.
     * @param action The action linking the source and target state.
     */
    void update(IScreenState source, IScreenState target, Action action);

    /**
     * Returns the states of the GUI model.
     *
     * @return Returns the states of the GUI model.
     */
    Set<IScreenState> getStates();

    /**
     * Checks whether the last inserted state is a new state.
     *
     * @return Returns {@code true} if the last inserted was a new state,
     *          otherwise {@code false} is returned.
     */
    boolean reachedNewState();

    /**
     * Returns the number of states in the GUI model.
     *
     * @return Returns the number of states in the GUI model.
     */
    int getNumberOfStates();

    /**
     * Returns the edges that are labeled by the given action.
     *
     * @param action The given action.
     * @return Returns the edges that are labeled by the given action.
     */
    Set<Edge> getEdges(Action action);

    /**
     * Returns a textual representation of the GUI model.
     *
     * @return Returns a textual representation of the GUI model.
     */
    String toString();

    // boolean containsState(IScreenState screenState);
    // void draw();
}
