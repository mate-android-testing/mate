package org.mate.commons.interaction.action.espresso.view_tree;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * A single step in a path.
 */
public class PathStep {

    private final PathStepType type;
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

    public static PathStep buildStepToParent() {
        return new PathStep(PathStepType.MOVE_TO_PARENT);
    }

    public static PathStep buildStepToChild(int childIndex) {
        return new PathStep(PathStepType.MOVE_TO_CHILD, childIndex);
    }

    public PathStepType getType() {
        return type;
    }

    public @Nullable EspressoViewTreeNode moveFromNode(EspressoViewTreeNode currentNode) {
        return moveFromNode(currentNode, PathWalkListener.EMPTY_LISTENER);
    }

    public @Nullable EspressoViewTreeNode moveFromNode(EspressoViewTreeNode currentNode, PathWalkListener listener) {
        switch (type) {
            case MOVE_TO_PARENT: {
                if (currentNode.hasParent()) {
                    EspressoViewTreeNode parentNode = currentNode.getParent();
                    listener.onMoveToParent(parentNode);
                    return parentNode;
                }

                return null;
            }
            case MOVE_TO_CHILD: {
                List<EspressoViewTreeNode> children = currentNode.getChildren();

                if (directionIndex < children.size()) {
                    EspressoViewTreeNode childNode = children.get(directionIndex);
                    listener.onMoveToChild(childNode);
                    return childNode;
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
