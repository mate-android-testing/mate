package org.mate.exploration.eda;

import android.support.annotation.NonNull;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.interaction.action.ui.UIAction;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class ClosestActionDistributionModel<Node extends UIAction> extends ProbabilityGraphDistributionModel<Node, Void> {
    private final Map<String, Integer> minActivityDistances; // min distance from activity a to target activity
    private final int maxDistance = Registry.getEnvironmentManager().getMaxActivityDistance();

    public ClosestActionDistributionModel(Node root, Map<String, Integer> minActivityDistances) {
        super(root);
        this.minActivityDistances = minActivityDistances;
        MATE.log(minActivityDistances.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining("\n")));
    }

    @Override
    public void update(Set<List<Node>> bestSequences) {
        Map<Node, Set<Node>> newEdges = getGraph(bestSequences);

        newEdges.forEach((from, to) -> edges.compute(from, (f, existingChildren) -> {
            if (existingChildren == null) {
                existingChildren = new HashMap<>();
            }

            for (Node n : to) {
                existingChildren.put(n, null);
            }

            return existingChildren;
        }));

        MATE.log("Distribution model after update: \n" + toString());
    }

    @Override
    protected Map<Node, Double> weightsToProbabilities(@NonNull Map<Node, Void> childrenWrapper) {
        Map<Node, Integer> weightedChildren = childrenWrapper.keySet().stream().collect(Collectors.toMap(Function.identity(), child -> minDistanceToTargetActivity(child).orElse(maxDistance)));
        int max = weightedChildren.values().stream().max(Integer::compare).orElseThrow(IllegalStateException::new);
        // Flip at max value to turn into a maximising problem
        ToIntFunction<Integer> alterValue = i -> max + 1 + max - i;

        double countSum = weightedChildren.values().stream().mapToInt(alterValue).sum();
        // + 1 to handle (distance == 0)
        return weightedChildren.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> alterValue.applyAsInt(e.getValue()) / countSum));
    }

    private Optional<Integer> minDistanceToTargetActivity(Node source) {
        Map<Node, Integer> distanceMap = new HashMap<>();
        distanceMap.put(source, 0);

        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();

        unsettledNodes.add(source);

        while (unsettledNodes.size() != 0) {
            Node currentNode = getLowestDistanceNode(unsettledNodes, distanceMap);
            unsettledNodes.remove(currentNode);
            if (edges.get(currentNode) != null) {
                for (Node adjacentNode: edges.get(currentNode).keySet()) {
                    Integer edgeWeight = minActivityDistances.getOrDefault(adjacentNode.getActivityName(), maxDistance);
                    if (!settledNodes.contains(adjacentNode)) {
                        calculateMinimumDistance(adjacentNode, edgeWeight, currentNode, distanceMap);
                        unsettledNodes.add(adjacentNode);
                    }
                }
            }
            settledNodes.add(currentNode);
        }

        return distanceMap.entrySet().stream().filter(e -> e.getKey() != source)
                .min(Comparator.comparingInt(e -> minActivityDistances.getOrDefault(e.getKey().getActivityName(), maxDistance)))
                .map(Map.Entry::getValue);
    }

    private Node getLowestDistanceNode(Set<Node> unsettledNodes, Map<Node, Integer> distanceMap) {
        Node lowestDistanceNode = null;
        int lowestDistance = Integer.MAX_VALUE;
        for (Node node: unsettledNodes) {
            int nodeDistance = distanceMap.getOrDefault(node, maxDistance);
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    private void calculateMinimumDistance(Node evaluationNode, Integer edgeWeigh, Node sourceNode, Map<Node, Integer> distanceMap) {
        Integer sourceDistance = distanceMap.getOrDefault(sourceNode, maxDistance);
        if (sourceDistance + edgeWeigh < distanceMap.getOrDefault(evaluationNode, Integer.MAX_VALUE)) {
            distanceMap.put(evaluationNode, sourceDistance + edgeWeigh);
        }
    }
}
