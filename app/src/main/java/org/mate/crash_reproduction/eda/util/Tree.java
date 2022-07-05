package org.mate.crash_reproduction.eda.util;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Tree<T> {
    private final TreeNode<T> root;

    public Tree(T root) {
        this.root = new TreeNode<>(root, null);
    }

    public TreeNode<T> getRoot() {
        return root;
    }

    public Optional<T> getParent(T elem) {
        Queue<TreeNode<T>> workQueue = new LinkedList<>();
        workQueue.add(root);

        while (!workQueue.isEmpty()) {
            TreeNode<T> node = workQueue.poll();

            if (node.getChild(c -> c.equals(elem)).isPresent()) {
                return Optional.of(node.getContent());
            } else {
                workQueue.addAll(node.getChildren());
            }
        }

        return Optional.empty();
    }

    public String toDot(Function<T, String> nodeLabel, Function<T, Map<String, String>> attributesGetter, BiFunction<TreeNode<T>, TreeNode<T>, Map<String, String>> edgeAttributesGetter) {
        Queue<TreeNode<T>> nodes = new LinkedList<>();
        nodes.add(root);
        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add("digraph D {");

        Function<TreeNode<T>, String> nodeToEscapedLabel = n -> '"' + nodeLabel.apply(n.getContent()) + " " + n.getParentList().map(p -> nodeLabel.apply(p.getContent())).collect(Collectors.joining(",")) + '"';
        Function<Map<String, String>, String> attributesToString = attributes -> attributes == null || attributes.isEmpty() ? ""
                : " [" + attributes.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", ")) + "]";

        while (!nodes.isEmpty()) {
            TreeNode<T> node = nodes.poll();

            Map<String, String> attributes = attributesGetter.apply(node.getContent());
            if (!attributes.containsKey("label")) {
                attributes.put("label", '"' + nodeLabel.apply(node.getContent()) + '"');
            }
            stringJoiner.add(nodeToEscapedLabel.apply(node) + attributesToString.apply(attributes));

            for (TreeNode<T> child : node.getChildren()) {
                nodes.add(child);

                stringJoiner.add(nodeToEscapedLabel.apply(node) + " -> " + nodeToEscapedLabel.apply(child) + attributesToString.apply(edgeAttributesGetter.apply(node, child)));
            }
        }

        stringJoiner.add("}");

        return stringJoiner.toString();
    }
}
