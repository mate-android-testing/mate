package org.mate.commons.interaction.action.espresso.view_tree;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import org.mate.commons.interaction.action.espresso.EspressoView;

import java.util.ArrayList;
import java.util.List;

public class EspressoViewTreeNode {
    private EspressoView espressoView;
    private EspressoViewTreeNode parent;
    private List<EspressoViewTreeNode> children = new ArrayList<>();

    public EspressoViewTreeNode(View espressoView) {
        this(espressoView, null);
    }

    public EspressoViewTreeNode(View espressoView, EspressoViewTreeNode parent) {
        this.espressoView = new EspressoView(espressoView);
        this.parent = parent;

        // if this node is a ViewGroup, load all children recursively
        if (espressoView instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) espressoView;
            int childCount = group.getChildCount();

            for (int i = 0; i < childCount; i++) {
                View child = group.getChildAt(i);
                children.add(new EspressoViewTreeNode(child, this));
            }
        }
    }

    public boolean hasParent() {
        return parent != null;
    }

    public EspressoViewTreeNode getParent() {
        return parent;
    }

    public List<EspressoViewTreeNode> getChildren() {
        return children;
    }

    /**
     * Perform a DFS to get the node corresponding to the requested view.
     *
     * @param targetView
     * @return
     */
    @Nullable
    public EspressoViewTreeNode findNode(EspressoView targetView) {
        if (espressoView.equals(targetView)) {
            // we are the corresponding node
            return this;
        }

        // look recursively in our children
        for (EspressoViewTreeNode child : children) {
            EspressoViewTreeNode result = child.findNode(targetView);
            if (result != null) {
                return result;
            }
        }

        // neither us nor any sub-node matches the requested target view.
        return null;
    }

    /**
     * Return the nodes "below" us in the tree (in preorder).
     */
    public List<EspressoViewTreeNode> getAllNodesInSubtree() {
        List<EspressoViewTreeNode> nodes = new ArrayList<>();

        // add ourselves
        nodes.add(this);

        // add the rest of the nodes
        for (EspressoViewTreeNode child : children) {
            nodes.addAll(child.getAllNodesInSubtree());
        }

        return nodes;
    }

    public EspressoView getEspressoView() {
        return espressoView;
    }
}
