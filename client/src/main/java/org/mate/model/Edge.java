package org.mate.model;

import org.mate.commons.interaction.action.Action;
import org.mate.state.IScreenState;

/**
 * Represents an edge that links the connection between two screen states by an action.
 * Or more simply, executing the action on the source screen state leads to the target
 * screen state.
 */
public class Edge {

    private Action action;
    private IScreenState source;
    private IScreenState target;

    public Edge(Action action, IScreenState source, IScreenState target) {
        this.action = action;
        this.source = source;
        this.target = target;
    }

    public Action getAction() {
        return action;
    }

    public IScreenState getSource() {
        return source;
    }

    public IScreenState getTarget() {
        return target;
    }
}
