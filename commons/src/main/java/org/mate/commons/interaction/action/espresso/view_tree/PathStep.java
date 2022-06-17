package org.mate.commons.interaction.action.espresso.view_tree;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * A single step in a path over a tree.
 */
public class PathStep {

    /**
     * The type of step: moving "up" to a parent, or moving "down" to a children.
     */
    private final PathStepType type;

    /**
     * The index this step should choose when moving "down" to a children.
     */
    private final int directionIndex;

    /**
     * Copy constructor
     * @param other
     */
    public PathStep(PathStep other) {
        this.type = other.type;
        this.directionIndex = other.directionIndex;
    }

    private PathStep(PathStepType type) {
        this(type, -1);
    }

    private PathStep(PathStepType type, int directionIndex) {
        this.type = type;
        this.directionIndex = directionIndex;
    }

    /**
     * Returns a Step for moving "up" to a parent.
     * @return a Step for moving "up" to a parent.
     */
    public static PathStep buildStepToParent() {
        return new PathStep(PathStepType.MOVE_TO_PARENT);
    }

    /**
     * Returns Step for moving "down" to a children.
     * @param childIndex the index of the children to move "down" to.
     * @return a Step for moving "down" to a children.
     */
    public static PathStep buildStepToChild(int childIndex) {
        return new PathStep(PathStepType.MOVE_TO_CHILD, childIndex);
    }

    /**
     * @return The type of step.
     */
    public PathStepType getType() {
        return type;
    }

    /**
     * Move from one node to another, according to this step.
     * @param node the node from which to move.
     * @return the node achieved after moving, or null if we were unable to complete the step.
     */
    public @Nullable EspressoViewTreeNode moveFromNode(EspressoViewTreeNode node) {
        switch (type) {
            case MOVE_TO_PARENT: {
                if (node.hasParent()) {
                    return node.getParent();
                }

                return null;
            }
            case MOVE_TO_CHILD: {
                List<EspressoViewTreeNode> children = node.getChildren();

                if (directionIndex < children.size()) {
                    return children.get(directionIndex);
                }

                return null;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathStep pathStep = (PathStep) o;
        return directionIndex == pathStep.directionIndex && type == pathStep.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, directionIndex);
    }
}
