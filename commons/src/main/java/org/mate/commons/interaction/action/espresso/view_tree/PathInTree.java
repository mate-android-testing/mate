package org.mate.commons.interaction.action.espresso.view_tree;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a path that can be traversed inside a tree.
 * Its composed of zero or more steps, which tells the traveller with direction to take at each node.
 */
public class PathInTree {

    /**
     * The steps in this path.
     */
    List<PathStep> steps = new ArrayList<>();

    public PathInTree() {}

    /**
     * Copy constructor.
     * @param other path
     */
    public PathInTree(PathInTree other) {
        for (PathStep step : other.steps) {
            steps.add(new PathStep(step));
        }
    }

    /**
     * Traverse the tree using this path, starting from a given node.
     * @param startingNode the node on which to start the path.
     * @return the node achieved after traversing, or null if we were unable to complete all steps
     * in path.
     */
    public @Nullable
    EspressoViewTreeNode walkPathFromNode(EspressoViewTreeNode startingNode) {
        EspressoViewTreeNode currentNode = startingNode;

        for (PathStep step : steps) {
            if (currentNode == null) {
                // we were unable to walk path completely
                return null;
            }

            currentNode = step.moveFromNode(currentNode);
        }

        return currentNode;
    }

    /**
     * Add a step that moves to the parent node at the end of this path.
     */
    public void addStepToParent() {
        steps.add(PathStep.buildStepToParent());
    }

    /**
     * Add a step that moves to a children node at the end of this path.
     * @param childIndex the index of the child node to move to.
     */
    public void addStepToChild(int childIndex) {
        steps.add(PathStep.buildStepToChild(childIndex));
    }

    /**
     * @return The steps in this path.
     */
    public List<PathStep> getSteps() {
        return steps;
    }

    /**
     * @return a boolean indicating whether this path has any steps or not.
     */
    public boolean isEmpty() {
        return steps.isEmpty();
    }

    /**
     * @return the first step of this path.
     */
    public PathStep getHead() {
        return steps.get(0);
    }

    /**
     * @return a copy of this path without its first step.
     */
    public PathInTree getTail() {
        PathInTree tail = new PathInTree(this);

        tail.steps.remove(0);

        return tail;
    }
}
