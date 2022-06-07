package org.mate.commons.interaction.action.espresso.view_tree;

import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the UI hierarchy of a specific screen.
 *
 * The special thing about this class is that it provides an iterator that starts from a target
 * view and iterates over all the views in the tree expanding from the target view.
 * That is, it will visit first the views in the tree closer to the target (e.g., children or
 * parent), and afterwards the more distant ones (e.g., children's children, parent's parent, etc.).
 */
public class EspressoViewTree {

    @Nullable
    private EspressoViewTreeNode root;

    public EspressoViewTree() {
        // empty tree
    }

    public EspressoViewTree(View root) {
        this.root = new EspressoViewTreeNode(root);
    }

    public List<EspressoViewTreeNode> getAllNodes() {
        if (root == null) {
            return new ArrayList<>();
        }

        return root.getAllNodesInSubtree();
    }

    public EspressoViewTreeIterator getTreeIteratorForTargetNode(EspressoViewTreeNode targetNode) {
        if (targetNode == null) {
            return new EspressoViewTreeIterator();
        }

        return new EspressoViewTreeIterator(targetNode);
    }

    public EspressoViewTreeNode findNodeForView(View view) {
        for (EspressoViewTreeNode node : this.getAllNodes()) {
            if (node.getEspressoView().getView().equals(view)) {
                return node;
            }
        }

        return null;
    }
}
