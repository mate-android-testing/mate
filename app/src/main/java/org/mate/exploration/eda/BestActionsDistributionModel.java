package org.mate.exploration.eda;

import android.support.annotation.NonNull;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Uses the best actions to update the weights. The more an action is used by tests the higher
 * the probability of picking the action again.
 * @param <Node> A node in the distribution model
 */
public class BestActionsDistributionModel<Node extends UIAction> extends ProbabilityGraphDistributionModel<Node, Double> {
    private final Set<String> targetActivities = new HashSet<>(Arrays.asList(Properties.TARGET().split(",")));
    public BestActionsDistributionModel(Node root) {
        super(root);
    }

    @Override
    public void update(Set<List<Node>> best) {
        for (List<Node> sequence : best) {
            double weightFactor = calculateSequenceWeightFactor(sequence);
            Map<Node, Set<Node>> newEdges = getGraph(sequence);

            newEdges.forEach((from, to) -> {
                edges.compute(from, (n, oldChildren) -> {
                    Map<Node, Double> children = oldChildren == null ? new HashMap<>() : oldChildren;
                    for (Node child : to) {
                        children.compute(child, (c, oldValue) -> ((oldValue == null ? 0 : oldValue) + calculateWeightGain(from, child)) * weightFactor);
                    }
                    return children;
                });
            });
        }

        MATE.log("Distribution model after update:");
        Arrays.stream(toString().split("\n")).forEach(MATE::log);
    }

    private double calculateWeightGain(Node from, Node to) {
        String startActivity = from.getActivityName().replaceAll("/", "");
        String endActivity = to.getActivityName().replaceAll("/", "");

        boolean startAtTarget = targetActivities.contains(startActivity);
        boolean endAtTarget = targetActivities.contains(endActivity);

        if (startAtTarget) {
            if (endAtTarget) {
                // GOOD
                return 5;
            } else {
                // BAD
                return 0.1;
            }
        } else {
            if (endAtTarget) {
                // VERY GOOD
                return 10;
            } else {
                // OK
                return 1;
            }
        }
    }

    private double calculateSequenceWeightFactor(List<Node> sequence) {
        return sequence.stream().map(UIAction::getActivityName)
                .map(s -> s.replaceAll("/", ""))
                .anyMatch(targetActivities::contains) ? 3 : 1;
    }

    @Override
    protected Map<Node, Double> weightsToProbabilities(@NonNull Map<Node, Double> children) {
        double countSum = children.values().stream().reduce(0D, Double::sum);

        return children.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / countSum));
    }

    @Override
    protected Optional<String> nodeColor(Node node) {
        return targetActivities.contains(node.getActivityName().replaceAll("/", "")) ? Optional.of("red") : super.nodeColor(node);
    }
}
