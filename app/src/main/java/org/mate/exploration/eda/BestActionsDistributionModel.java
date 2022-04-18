package org.mate.exploration.eda;

import android.annotation.SuppressLint;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.utils.Randomness;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Uses the best actions to update the weights. The more an action is used by tests the higher
 * the probability of picking the action again.
 * @param <Node> A node in the distribution model
 */
public class BestActionsDistributionModel<Node extends Action> implements IDistributionModel<Node> {
    private final Node root;
    private final Map<Node, Map<Node, Integer>> edges = new HashMap<>();

    public BestActionsDistributionModel(Node root) {
        this.root = root;
    }

    public void update(Set<List<Node>> best) {
        Map<Node, Set<Node>> newEdges = getGraph(best);
        Queue<Node> workQueue = new LinkedList<>();
        Set<Node> visitedNodes = new HashSet<>();
        workQueue.add(root);

        while (!workQueue.isEmpty()) {
            Node node = workQueue.poll();
            visitedNodes.add(node);
            Set<Node> newChildren = newEdges.getOrDefault(node, new HashSet<>());

            edges.compute(node, (n, oldChildren) -> {
                Map<Node, Integer> children = oldChildren == null ? new HashMap<>() : oldChildren;
                for (Node child : newChildren) {
                    children.compute(child, (c, oldValue) -> (oldValue == null ? 0 : oldValue) + 1);
                }
                return children;
            });

            workQueue.addAll(newChildren);
            workQueue.removeIf(visitedNodes::contains);
        }

        MATE.log("Distribution model after update: \n" + printModel());
        MATE.log("Best after update: \n" + getBest(root).stream().map(Action::toShortString).collect(Collectors.joining(", ")));
    }

    private Map<Node, Set<Node>> getGraph(Set<List<Node>> nodeLists) {
        // TODO this looses duplicates -> if every node list starts with the same action maybe we should give that more weight
        Map<Node, Set<Node>> graph = new HashMap<>();

        for (List<Node> list : nodeLists) {
            Iterator<Node> iterator = list.iterator();
            Node prev = root;

            while (iterator.hasNext()) {
                Node cur = iterator.next();
                graph.compute(prev, (key, oldValue) -> {
                    Set<Node> nodes = oldValue == null ? new HashSet<>() : oldValue;
                    nodes.add(cur);

                    return nodes;
                });
                prev = cur;
            }
        }

        return graph;
    }

    public List<Node> drawSample() {
        List<Node> nodes = new LinkedList<>();
        Optional<Node> next = drawNextNode();

        while (next.isPresent()) {
            nodes.add(next.get());
            next = drawNextNode(next.get());
        }

        MATE.log("Picked actions: " + nodes.stream().map(Node::toShortString).collect(Collectors.joining(", ")));

        return nodes;
    }

    public Optional<Node> drawNextNode() {
        return drawNextNode(root);
    }

    public Optional<Node> drawNextNode(Node start) {
        Map<Node, Integer> children = edges.get(start == null ? root : start);

        if (children == null || children.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(Randomness.randomIndexWithProbabilities(countsToProbability(children)));
        }
    }

    public List<Node> getBest(Node start) {
        start = start == null ? root : start;
        List<Node> bestPath = new LinkedList<>();

        Map<Node, Integer> children = edges.get(start);

        while (children != null && !children.isEmpty()) {
            start = children.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();
            children = edges.get(start);

            if (bestPath.contains(start)) {
                return bestPath;
            }
            bestPath.add(start);
        }

        return bestPath;
    }

    @SuppressLint("DefaultLocale")
    public String printModel() {
        StringJoiner graph = new StringJoiner("\n");

        graph.add("digraph G {");

        for (Node node : edges.keySet()) {
            Map<Node, Integer> nodesToCount = edges.get(node);
            double countSum = nodesToCount.values().stream().reduce(0, Integer::sum);

            nodesToCount.entrySet().stream().forEach(e -> graph.add(String.format(
                    "\"%s\" -> \"%s\" [ label=\"abs: %d, rel: %f\" ];",
                    node.toShortString(),
                    e.getKey().toShortString(),
                    e.getValue(),
                    (double) e.getValue() / countSum
            )));
        }

        graph.add("}");

        return graph.toString();
    }

    private Map<Node, Double> countsToProbability(Map<Node, Integer> nodeToCount) {
        if (nodeToCount == null) {
            return new HashMap<>();
        }

        double countSum = nodeToCount.values().stream().reduce(0, Integer::sum);

        return nodeToCount.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / countSum));
    }
}
