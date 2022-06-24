package org.mate.crash_reproduction.eda.util;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreeNode<T> {
    private final T content;
    private final TreeNode<T> parent;
    private final Set<TreeNode<T>> children = new HashSet<>();

    public TreeNode(T content, TreeNode<T> parent) {
        this.content = content;
        this.parent = parent;
    }

    public TreeNode<T> addChild(T childContent) {
        TreeNode<T> node = new TreeNode<>(childContent, this);
        children.add(node);
        return node;
    }

    public Set<TreeNode<T>> getChildren() {
        return children;
    }

    public Stream<TreeNode<T>> getParentList() {
        if (parent == null) {
            return Stream.empty();
        } else {
            return Stream.concat(Stream.of(parent), parent.getParentList());
        }
    }

    public Optional<TreeNode<T>> getChild(Predicate<T> predicate) {
        List<TreeNode<T>> matchingChildren = children.stream().filter(n -> predicate.test(n.content)).collect(Collectors.toList());

        if (matchingChildren.size() > 1) {
            throw new IllegalStateException("More than one child matched predicate!");
        } else {
            return matchingChildren.stream().findAny();
        }
    }

    public T getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode<?> treeNode = (TreeNode<?>) o;
        return Objects.equals(children, treeNode.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(children);
    }
}
