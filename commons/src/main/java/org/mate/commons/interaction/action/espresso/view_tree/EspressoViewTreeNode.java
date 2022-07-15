package org.mate.commons.interaction.action.espresso.view_tree;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import org.mate.commons.interaction.action.espresso.EspressoView;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in an EspressoViewTree.
 * I.e., a node that represents a view in the UI Hierarchy.
 */
public class EspressoViewTreeNode {

    /**
     * The EspressoView contained in this node.
     */
    private final EspressoView espressoView;

    /**
     * The parent node of this node.
     */
    private @Nullable
    final EspressoViewTreeNode parent;

    /**
     * The children nodes of this node.
     */
    private final List<EspressoViewTreeNode> children = new ArrayList<>();

    public EspressoViewTreeNode(View espressoView, String activityName) {
        this(espressoView, activityName, null);
    }

    public EspressoViewTreeNode(View espressoView, String activityName,
                                @Nullable EspressoViewTreeNode parent) {
        this.espressoView = new EspressoView(espressoView, activityName);
        this.parent = parent;

        // if this node is a ViewGroup, load all children recursively
        if (espressoView instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) espressoView;
            int childCount = group.getChildCount();

            for (int i = 0; i < childCount; i++) {
                View child = group.getChildAt(i);
                children.add(new EspressoViewTreeNode(child, activityName, this));
            }
        }
    }

    /**
     * @return the EspressoView contained in this node.
     */
    public EspressoView getEspressoView() {
        return espressoView;
    }

    /**
     * @return a boolean indicating whether this node has a parent or not.
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * @return the parent node of this node.
     */
    public @Nullable EspressoViewTreeNode getParent() {
        return parent;
    }

    /**
     * @return the children nodes of this node.
     */
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
     * @return the nodes "below" this node in the tree (in preorder).
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
}
