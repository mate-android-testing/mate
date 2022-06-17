package org.mate.commons.interaction.action.espresso.view_tree;

import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the UI hierarchy of a specific screen.
 *
 * The special thing about this class is that it provides an iterator that starts from a certain
 * view and iterates over all the views in the tree expanding from the starting view.
 * That is, it will visit first the views in the tree closer to the target (e.g., children or
 * parent), and afterwards the more distant ones (e.g., children's children, parent's parent, etc.).
 */
public class EspressoViewTree {

    /**
     * The root node in the tree.
     */
    @Nullable
    private EspressoViewTreeNode root;

    public EspressoViewTree() {
        // empty tree
    }

    public EspressoViewTree(View root, String activityName) {
        this.root = new EspressoViewTreeNode(root, activityName);
    }

    /**
     * @return all nodes in the tree.
     */
    public List<EspressoViewTreeNode> getAllNodes() {
        if (root == null) {
            return new ArrayList<>();
        }

        return root.getAllNodesInSubtree();
    }

    /**
     * Returns an iterator that start at a certain node and then traverses the whole ViewTree.
     * It will visit first the views closer to the starting node.
     * @param startingNode the node from which to start the iterator
     * @return an iterator
     */
    public EspressoViewTreeIterator getTreeIteratorForTargetNode(EspressoViewTreeNode startingNode) {
        if (startingNode == null) {
            return new EspressoViewTreeIterator();
        }

        return new EspressoViewTreeIterator(startingNode);
    }

    /**
     * Find the node in the tree corresponding to a view.
     * @param view to find.
     * @return the found node, null otherwise.
     */
    public @Nullable EspressoViewTreeNode findNodeForView(View view) {
        for (EspressoViewTreeNode node : this.getAllNodes()) {
            if (node.getEspressoView().getView().equals(view)) {
                return node;
            }
        }

        return null;
    }
}
