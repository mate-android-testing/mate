package org.mate.model;

import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(action, edge.action) && Objects.equals(source, edge.source) && Objects.equals(target, edge.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, source, target);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "action=" + action.toShortString() +
                ", source=" + source +
                ", target=" + target +
                '}';
    }
}
