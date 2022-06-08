package org.mate.model;

import org.mate.commons.interaction.action.Action;
import org.mate.state.IScreenState;

import java.util.List;
import java.util.Optional;
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
     * Adds a root state to the gui model. Root states are the states that can be initial states
     * of an AUT after the AUT is restarted.
     *
     * @param rootState A new root state.
     */
    void addRootState(IScreenState rootState);

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
     * Returns the edges contained in the GUI model.
     *
     * @return Returns the edges between the GUI states.
     */
    Set<Edge> getEdges();

    /**
     * Tries to find the shortest path between two given states.
     *
     * @param from The source state.
     * @param to The target state.
     * @return Returns the shortest path between the source and target state if such path exists.
     */
    Optional<List<Edge>> shortestPath(IScreenState from, IScreenState to);

    /**
     * Returns the screen state matching the given id.
     *
     * @param screenStateId The screen state id.
     * @return Returns the screen state matching the given id or {@code null} if no such screen state
     *          exists.
     */
    IScreenState getScreenStateById(String screenStateId);

    /**
     * Returns the root states, i.e. the start screens of the AUT.
     *
     * @return Returns the start screen states.
     */
    Set<IScreenState> getRootStates();

    /**
     * Returns the screen states describing the given activity.
     *
     * @return Returns the screen states describing the given activity.
     */
    Set<IScreenState> getActivityStates(String activity);

    /**
     * Returns the screen states belonging to the AUT.
     *
     * @return Returns the screen states of the AUT.
     */
    Set<IScreenState> getAppStates();

    /**
     * Returns the activity predecessors of the given activity.
     *
     * @param activity The given activity.
     * @return Returns the activities that have a direct transition to the given activity.
     */
    Set<String> getActivityPredecessors(String activity);

    /**
     * Returns a textual representation of the GUI model.
     *
     * @return Returns a textual representation of the GUI model.
     */
    String toString();

    // boolean containsState(IScreenState screenState);
    // void draw();
}
