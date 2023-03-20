package org.mate.exploration.genetic.util.eda.pipe.ppt;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

/**
 * An abstract tree representation.
 *
 * @param <T> The type of a tree node.
 */
public class Tree<T> {

    /**
     * The root node.
     */
    private final TreeNode<T> root;

    /**
     * Constructs a new tree with the given root node.
     *
     * @param root The root node.
     */
    public Tree(T root) {
        this.root = new TreeNode<>(root, null);
    }

    /**
     * Retrieves the root node.
     *
     * @return Returns the root node.
     */
    public TreeNode<T> getRoot() {
        return root;
    }

    /**
     * Retrieves the parent element of the given element.
     *
     * @param elem The element for which the parent element should be derived.
     * @return Returns the parent element.
     */
    public Optional<T> getParent(T elem) {

        final Queue<TreeNode<T>> workQueue = new LinkedList<>();
        workQueue.add(root);

        while (!workQueue.isEmpty()) {
            final TreeNode<T> node = workQueue.poll();

            if (node.getChild(c -> c.equals(elem)).isPresent()) {
                return Optional.of(node.getContent());
            } else {
                workQueue.addAll(node.getChildren());
            }
        }

        return Optional.empty();
    }
}
