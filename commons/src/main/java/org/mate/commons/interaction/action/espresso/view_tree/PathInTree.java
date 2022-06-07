package org.mate.commons.interaction.action.espresso.view_tree;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PathInTree {

    List<PathStep> steps = new ArrayList<>();

    public PathInTree() {}

    /**
     * Copy constructor.
     * @param other
     */
    public PathInTree(PathInTree other) {
        for (PathStep step : other.steps) {
            steps.add(new PathStep(step));
        }
    }

    public @Nullable
    EspressoViewTreeNode walkPathFromNode(EspressoViewTreeNode startingNode) {
        return walkPathFromNode(startingNode, PathWalkListener.EMPTY_LISTENER);
    }

    public @Nullable
    EspressoViewTreeNode walkPathFromNode(EspressoViewTreeNode startingNode, PathWalkListener listener) {
        EspressoViewTreeNode currentNode = startingNode;

        for (PathStep step : steps) {
            if (currentNode == null) {
                // we were unable to walk path completely
                return null;
            }

            currentNode = step.moveFromNode(currentNode, listener);
        }

        listener.onPathCompleted(currentNode);

        return currentNode;
    }

    public void addStepToParent() {
        steps.add(PathStep.buildStepToParent());
    }

    public void addStepToChild(int childIndex) {
        steps.add(PathStep.buildStepToChild(childIndex));
    }

    public List<PathStep> getSteps() {
        return steps;
    }

    public PathInTree getTail() {
        PathInTree tail = new PathInTree(this);

        tail.steps.remove(0);

        return tail;
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    public PathStep getHead() {
        return steps.get(0);
    }
}
