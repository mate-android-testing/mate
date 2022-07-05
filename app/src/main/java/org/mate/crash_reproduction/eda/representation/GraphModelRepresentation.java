package org.mate.crash_reproduction.eda.representation;

import org.mate.Registry;
import org.mate.crash_reproduction.eda.util.DotGraphUtil;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GraphModelRepresentation<ExtraInfo> implements IModelRepresentation {
    private final BiFunction<List<Action>, IScreenState, Map<Action, Double>> initializeNode;
    private final ExtraInfoGetter<ExtraInfo> extraInfoExtraInfoGetter;

    private final Map<Node, Map<Action, Double>> probabilities = new HashMap<>();
    private final Set<Edge> edges = new HashSet<>();
    private final Node root;

    public GraphModelRepresentation(ExtraInfoGetter<ExtraInfo> extraInfoExtraInfoGetter, BiFunction<List<Action>, IScreenState, Map<Action, Double>> initializeNode) {
        this.extraInfoExtraInfoGetter = extraInfoExtraInfoGetter;
        this.initializeNode = initializeNode;
        IScreenState rootState = Registry.getUiAbstractionLayer().getLastScreenState();
        root = new Node(rootState, extraInfoExtraInfoGetter.get(null, rootState));
        probabilities.put(root, initializeNode.apply(Collections.emptyList(), root.state));
    }

    @Override
    public void resetProbabilities() {
        probabilities.clear();
    }

    @Override
    public ModelRepresentationIterator getIterator() {
        return new RepresentationIterator();
    }

    private class RepresentationIterator implements ModelRepresentationIterator {
        private Node node = root;

        @Override
        public Map<Action, Double> getActionProbabilities() {
            return Objects.requireNonNull(probabilities.get(node));
        }

        @Override
        public IScreenState getState() {
            return node.state;
        }

        @Override
        public void updatePosition(TestCase testCase, Action action, IScreenState currentScreenState) {
            Node nextNode = new Node(currentScreenState, extraInfoExtraInfoGetter.get(node.state, currentScreenState));
            edges.add(new Edge(action, node, nextNode));
            node = nextNode;

            if (!probabilities.containsKey(node)) {
                probabilities.put(node, initializeNode.apply(testCase.getEventSequence(), currentScreenState));
            }
        }

        @Override
        public void updatePositionImmutable(IScreenState currentScreenState) {
            Node nextNode = new Node(currentScreenState, extraInfoExtraInfoGetter.get(node.state, currentScreenState));
            if (!probabilities.containsKey(nextNode)) {
                throw new IllegalStateException();
            }

            node = nextNode;
        }
    }

    private List<Edge> bestPath() {
        Set<Node> reachedNodes = new HashSet<>();
        List<Edge> path = new LinkedList<>();
        Node prevNode = root;
        Optional<Node> nextNode;

        do {
            reachedNodes.add(prevNode);
            Action nextAction = probabilities.get(prevNode).entrySet().stream()
                    .max(Comparator.comparingDouble(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElseThrow(IllegalArgumentException::new);

            Node finalPrevNode = prevNode;
            nextNode = edges.stream()
                    .filter(e -> e.action.equals(nextAction) && e.source.equals(finalPrevNode)).findFirst().map(e -> e.target);

            if (nextNode.isPresent()) {
                path.add(new Edge(nextAction, prevNode, nextNode.get()));
                prevNode = nextNode.get();
            }
        } while (nextNode.isPresent() && !reachedNodes.contains(nextNode.get()));

        return path;
    }

    @Override
    public String toString() {
        Function<Node, String> nodeToString = n -> '"' + n.state.getId() + "(" + (n.extraInfo == null ? null : n.extraInfo.toString()) + ")\"";
        List<Edge> bestPath = bestPath();

        Map<Node, Map<Action, Node>> edgeMap = new HashMap<>();

        for (Edge edge : this.edges) {
            edgeMap.computeIfAbsent(edge.source, e -> new HashMap<>())
                    .put(edge.action, edge.target);
        }

        StringJoiner graph = new StringJoiner("\n");
        graph.add("digraph D {");

        for (Map.Entry<Node, Map<Action, Node>> node : edgeMap.entrySet()) {
            Node source = node.getKey();
            Map<Action, Double> actionProbabilities = probabilities.get(source);
            Map<Action, Node> nodeEdges = node.getValue();
            Set<Map.Entry<Action, Double>> missingEdges = new HashSet<>();
            Map<Node, Set<Map.Entry<Action, Double>>> targetMap = new HashMap<>();

            for (Map.Entry<Action, Double> actionProbability : actionProbabilities.entrySet()) {
                Node target = nodeEdges.get(actionProbability.getKey());

                if (target == null) {
                    missingEdges.add(actionProbability);
                } else {
                    targetMap.computeIfAbsent(target, a -> new HashSet<>()).add(actionProbability);
                }
            }
            Map<String, String> nodeAttributes = DotGraphUtil.toDotNodeAttributeLookup(source.state, missingEdges, actionProbabilities);

            graph.add(nodeToString.apply(source) + " [" + DotGraphUtil.getAttributeString(nodeAttributes) + "]");

            for (Map.Entry<Node, Set<Map.Entry<Action, Double>>> targetEntry : targetMap.entrySet()) {
                Node target = targetEntry.getKey();
                Set<Map.Entry<Action, Double>> incomingEdges = targetEntry.getValue();

                boolean edgeOnBestPath = bestPath.stream().anyMatch(e -> e.source.equals(source) && e.target.equals(target));
                Map<String, String> edgeAttributes = DotGraphUtil.toDotEdgeAttributeLookup(actionProbabilities, incomingEdges, edgeOnBestPath);

                graph.add(nodeToString.apply(source) + " -> " + nodeToString.apply(target) + " [" + DotGraphUtil.getAttributeString(edgeAttributes) + "]");
            }
        }

        graph.add("}");

        return graph.toString();
    }

    private class Node {
        private final IScreenState state;
        private final ExtraInfo extraInfo;

        private Node(IScreenState state, ExtraInfo extraInfo) {
            this.state = state;
            this.extraInfo = extraInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(state, node.state) && Objects.equals(extraInfo, node.extraInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, extraInfo);
        }
    }

    private class Edge {
        private final Action action;
        private final Node source;
        private final Node target;

        private Edge(Action action, Node source, Node target) {
            this.action = action;
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return Objects.equals(action, edge.action) && Objects.equals(source, edge.source) && Objects.equals(target, edge.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(action, source, target);
        }
    }

    @FunctionalInterface
    public interface ExtraInfoGetter<ExtraInfo> {
        ExtraInfo get(IScreenState prevScreenState, IScreenState currentScreenState);
    }
}
