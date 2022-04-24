package org.mate.exploration.eda;

import android.support.annotation.NonNull;

import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.utils.Randomness;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ProbabilityGraphDistributionModel<Node, Weight> implements IDistributionModel<Node> {
    protected final Map<Node, Map<Node, Weight>> edges = new HashMap<>();
    protected final Node root;

    public ProbabilityGraphDistributionModel(Node root) {
        this.root = root;
    }

    @Override
    public Optional<Node> drawNextNode(Node startNode) {
        return Optional.ofNullable(edges.get(startNode))
                .filter(children -> !children.isEmpty())
                .map(this::weightsToProbabilities)
                .map(Randomness::randomIndexWithProbabilities);
    }

    public List<Node> getBestSequence(Node start) {
        start = start == null ? root : start;
        List<Node> bestPath = new LinkedList<>();

        Optional<Node> nextBest = getNextBestNode(start);
        while (nextBest.isPresent()) {
            Node nextBestNode = nextBest.get();

            if (bestPath.contains(nextBestNode)) {
                break;
            } else {
                bestPath.add(nextBestNode);
                nextBest = getNextBestNode(nextBestNode);
            }
        }

        return bestPath;
    }

    public Optional<Node> getNextBestNode(Node start) {
        return Optional.ofNullable(edges.get(start))
                .map(this::weightsToProbabilities)
                .flatMap(children -> children.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)))
                .map(Map.Entry::getKey);
    }

    protected abstract Map<Node, Double> weightsToProbabilities(@NonNull Map<Node, Weight> children);

    @Override
    public String toString() {
        StringJoiner graph = new StringJoiner("\n");

        graph.add("digraph G {");

        getAllNodes().forEach(node -> nodeColor(node).ifPresent(color -> graph.add(String.format("\"%s\" [color = %s]", nodeToString(node), color))));

        for (Map.Entry<Node, Map<Node, Weight>> entry : edges.entrySet()) {
            Map<Node, Weight> children = entry.getValue();
            Map<Node, Double> childrenProbabilities = weightsToProbabilities(children);

            children.entrySet().stream().forEach(e -> graph.add(String.format(Locale.getDefault(),
                    "\"%s\" -> \"%s\" [ label=\"abs: %s, rel: %f\" ];",
                    nodeToString(entry.getKey()),
                    nodeToString(e.getKey()),
                    e.getValue(),
                    childrenProbabilities.get(e.getKey())
            )));
        }

        graph.add("}");

        return graph.toString();
    }

    protected Optional<String> nodeColor(Node node) {
        if (node == root) {
            return Optional.of("black");
        }
        return Optional.empty();
    }

    protected String nodeToString(Node node) {
        if (node instanceof UIAction) {
            return ((UIAction) node).toShortString() + " (" + node.hashCode() + ") on " + ((UIAction) node).getActivityName();
        }
        return node.toString();
    }

    protected Map<Node, Set<Node>> getGraph(List<Node> nodeLists) {
        Map<Node, Set<Node>> graph = new HashMap<>();

        Iterator<Node> iterator = nodeLists.iterator();
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

        return graph;
    }

    protected Map<Node, Set<Node>> getGraph(Set<List<Node>> nodeLists) {
        return nodeLists.stream().map(this::getGraph)
                .reduce(new HashMap<>(), this::merge);
    }

    private Map<Node, Set<Node>> merge(Map<Node, Set<Node>> acc, Map<Node, Set<Node>> cur) {
        cur.forEach((from, to) -> {
            acc.computeIfAbsent(from, f -> new HashSet<>()).addAll(to);
        });
        return acc;
    }

    private Stream<Node> getAllNodes() {
        return Stream.concat(edges.keySet().stream(), edges.values().stream().flatMap(a -> a.keySet().stream())).distinct();
    }
}
