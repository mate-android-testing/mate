package org.mate.exploration.genetic.util.eda;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An abstract tree node for {@link Tree}.
 *
 * @param <T> The element type encapsulated by a tree node.
 */
public class TreeNode<T> {

    /**
     * The element encapsulated by a tree node.
     */
    private final T content;

    /**
     * The parent node.
     */
    private final TreeNode<T> parent;

    /**
     * The children nodes.
     */
    private final Set<TreeNode<T>> children = new HashSet<>();

    /**
     * Constructs a new tree node.
     *
     * @param content The element encapsulated by the tree node.
     * @param parent The parent node.
     */
    public TreeNode(T content, TreeNode<T> parent) {
        this.content = content;
        this.parent = parent;
    }

    /**
     * Adds a new child node.
     *
     * @param childContent The element encapsulated by the child node.
     * @return Returns the new child node.
     */
    public TreeNode<T> addChild(T childContent) {
        TreeNode<T> node = new TreeNode<>(childContent, this);
        children.add(node);
        return node;
    }

    /**
     * Retrieves the children nodes.
     *
     * @return Returns the children nodes.
     */
    public Set<TreeNode<T>> getChildren() {
        return children;
    }

    /**
     * Returns the list of parent nodes.
     *
     * @return Returns the list of parent nodes.
     */
    public Stream<TreeNode<T>> getParentList() {
        if (parent == null) {
            return Stream.empty();
        } else {
            return Stream.concat(Stream.of(parent), parent.getParentList());
        }
    }

    /**
     * Retrieves the child node that matches the given predicate. Note that the predicate must be
     * unique, i.e. it must not match for multiple child nodes.
     *
     * @param predicate The given predicate.
     * @return Returns the child node matching the given predicate if such node exists.
     */
    public Optional<TreeNode<T>> getChild(final Predicate<T> predicate) {

        final List<TreeNode<T>> matchingChildren = children.stream()
                .filter(n -> predicate.test(n.content)).collect(Collectors.toList());

        if (matchingChildren.size() > 1) {
            throw new IllegalStateException("More than one child matched predicate!");
        } else {
            return matchingChildren.stream().findAny();
        }
    }

    /**
     * Returns the element encapsulated by the tree node.
     *
     * @return Returns the element encapsulated by the tree node.
     */
    public T getContent() {
        return content;
    }

    /**
     * Checks for equality between two tree nodes.
     *
     * @param o The other tree node.
     * @return Returns {@code true} if the tree node elements are identical and they have the same
     *          node parent node, otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode<?> treeNode = (TreeNode<?>) o;
        return Objects.equals(content, treeNode.content) && Objects.equals(parent, treeNode.parent);
    }

    /**
     * Computes the hash code for the tree node.
     *
     * @return Returns the hash code for the tree node.
     */
    @Override
    public int hashCode() {
        return Objects.hash(content, parent);
    }
}
