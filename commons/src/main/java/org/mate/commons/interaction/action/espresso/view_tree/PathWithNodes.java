package org.mate.commons.interaction.action.espresso.view_tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Auxiliary class that wraps a Path in a ViewTree, with the starting and ending nodes.
 */
public class PathWithNodes {

    /**
     * The starting node before traversing the path.
     */
    private final EspressoViewTreeNode startingNode;

    /**
     * The path to traverse from the starting node.
     */
    private final PathInTree path;

    /**
     * The ending node after traversing the path.
     */
    private final EspressoViewTreeNode endingNode;

    private PathWithNodes(EspressoViewTreeNode node) {
        this(node, new PathInTree(), node);
    }

    private PathWithNodes(EspressoViewTreeNode startingNode,
                          PathInTree path,
                          EspressoViewTreeNode endingNode) {
        this.startingNode = startingNode;
        this.path = path;
        this.endingNode = endingNode;
    }

    /**
     * @return The starting node before traversing the path.
     */
    public EspressoViewTreeNode getStartingNode() {
        return endingNode;
    }

    /**
     * @return The path to traverse from the starting node.
     */
    public PathInTree getPath() {
        return path;
    }

    /**
     * @return The ending node after traversing the path.
     */
    public EspressoViewTreeNode getEndingNode() {
        return endingNode;
    }

    /**
     * Build a PathWithNodes instance that has an empty path.
     * I.e., starting and ending node is the same one.
     */
    public static PathWithNodes buildWithEmptyPath(EspressoViewTreeNode node) {
        return new PathWithNodes(node);
    }

    /**
     * Returns a list of nodes containing the parent and children (a.k.a. node neighbors) of the
     * ending node.
     * @return a list of nodes
     */
    public List<PathWithNodes> getEndingNodeNeighbors() {
        List<PathWithNodes> neighbors = new ArrayList<>();

        if (endingNode.hasParent()) {
            PathInTree newPath = new PathInTree(path);
            newPath.addStepToParent();
            neighbors.add(new PathWithNodes(startingNode, newPath, endingNode.getParent()));
        }

        List<EspressoViewTreeNode> children = endingNode.getChildren();
        for (int i = 0; i < children.size(); i++) {
            PathInTree newPath = new PathInTree(path);
            newPath.addStepToChild(i);
            neighbors.add(new PathWithNodes(startingNode, newPath, children.get(i)));
        }

        return neighbors;
    }
}
