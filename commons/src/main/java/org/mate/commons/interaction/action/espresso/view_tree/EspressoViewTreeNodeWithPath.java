package org.mate.commons.interaction.action.espresso.view_tree;

import java.util.ArrayList;
import java.util.List;

public class EspressoViewTreeNodeWithPath {

    private EspressoViewTreeNode node;
    private PathInTree pathFromTarget;

    private EspressoViewTreeNodeWithPath(EspressoViewTreeNode node) {
        this(node, new PathInTree());
    }

    private EspressoViewTreeNodeWithPath(EspressoViewTreeNode node, PathInTree pathFromTarget) {
        this.node = node;
        this.pathFromTarget = pathFromTarget;
    }

    public EspressoViewTreeNode getNode() {
        return node;
    }

    public static EspressoViewTreeNodeWithPath buildWithEmptyPath(EspressoViewTreeNode node) {
        return new EspressoViewTreeNodeWithPath(node);
    }

    /**
     * Return a list containing the parent and children of a node (a.k.a. node neighbors).
     *
     * @return
     */
    public List<EspressoViewTreeNodeWithPath> getNeighbors() {
        List<EspressoViewTreeNodeWithPath> neighbors = new ArrayList<>();

        if (node.hasParent()) {
            PathInTree newPath = new PathInTree(pathFromTarget);
            newPath.addStepToParent();
            neighbors.add(new EspressoViewTreeNodeWithPath(node.getParent(), newPath));
        }

        List<EspressoViewTreeNode> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            PathInTree newPath = new PathInTree(pathFromTarget);
            newPath.addStepToChild(i);
            neighbors.add(new EspressoViewTreeNodeWithPath(children.get(i), newPath));
        }

        return neighbors;
    }

    public PathInTree getPathFromTarget() {
        return pathFromTarget;
    }
}
