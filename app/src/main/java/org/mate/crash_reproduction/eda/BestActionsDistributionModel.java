package org.mate.crash_reproduction.eda;

import android.support.annotation.NonNull;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Uses the best actions to update the weights. The more an action is used by tests the higher
 * the probability of picking the action again.
 * @param <Node> A node in the distribution model
 */
public class BestActionsDistributionModel<Node extends UIAction> extends ProbabilityGraphDistributionModel<Node, Double> {
    private final BranchDistanceFitnessFunction<TestCase> fitnessFunction = new BranchDistanceFitnessFunction<>();
    private final Set<String> targetActivitiesOrFragments = Registry.getEnvironmentManager().getTargetActivities();

    public BestActionsDistributionModel(Node root) {
        super(root);
    }

    @Override
    public void update(Set<IChromosome<TestCase>> best) {
        for (IChromosome<TestCase> testCase : best) {
            List<Node> sequence = testCase.getValue().getEventSequence().stream().map(a -> (Node) a).collect(Collectors.toList());
            double fitness = fitnessFunction.getNormalizedFitness(testCase);
            double weightFactor = fitnessFunction.isMaximizing() ? fitness : (1 - fitness);
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

            List<List<Node>> sequenceToTargetActivity = sequencesUntilReachedTargetActivity(sequence);

            for (List<Node> sequenceToTarget : sequenceToTargetActivity) {
                Map<Node, Set<Node>> greatSequence = getGraph(sequenceToTarget);

                greatSequence.forEach((from, to) -> {
                    Map<Node, Double> children = edges.computeIfAbsent(from, n -> new HashMap<>());
                    for (Node child : to) {
                        children.compute(child, (c, oldValue) -> (oldValue == null ? 0 : oldValue) + 5);
                    }
                });
            }
        }

        MATE.log("Distribution model after update:");
        Arrays.stream(toString().split("\n")).forEach(MATE::log);

        List<Node> bestSequence = getBestSequence(root);
        MATE.log("BEST SEQUENCE STATS:");
        MATE.log("Length: " + bestSequence.size());
        List<List<Node>> sequenceToTarget = sequencesUntilReachedTargetActivity(bestSequence);
        MATE.log("Sequences to target" + sequenceToTarget.size());
        MATE.log("Any node reached target: " + getAllNodes().filter(this::reachedTarget).findAny().map(Node::toShortString).orElse("-"));
    }

    private List<List<Node>> sequencesUntilReachedTargetActivity(List<Node> fullSequence) {
        List<List<Node>> sequences = new LinkedList<>();
        List<Node> nodes = new LinkedList<>();
        for (Node node : fullSequence) {
            nodes.add(node);

            if (reachedTarget(node)) {
                sequences.add(nodes);
                nodes = new LinkedList<>();
            }
        }
        return sequences;
    }

    private double calculateWeightGain(Node from, Node to) {
        boolean startedAtTarget = reachedTarget(from);
        boolean endedAtTarget = reachedTarget(to);

        if (startedAtTarget) {
            if (endedAtTarget) {
                // Stayed on target
                return 5;
            } else {
                // Left target
                return 0.1;
            }
        } else {
            if (endedAtTarget) {
                // Discovered target
                return 10;
            } else {
                // Boring
                return 1;
            }
        }
    }

    @Override
    protected Optional<String> getImage(Node node) {
        if (node.getScreenStateId() == null || node.getScreenStateId().trim().isEmpty()) {
            return super.getImage(node);
        }
        return Optional.of("results/pictures/" + Registry.getPackageName() + "/" + node.getScreenStateId() + ".png");
    }

    private boolean reachedTarget(Node node) {
        return targetActivitiesOrFragments.contains(node.getActivityName())
                || node.getFragmentNames().stream().anyMatch(fragmentName -> targetActivitiesOrFragments.stream().anyMatch(component -> component.contains(fragmentName)));
    }

    @Override
    protected Map<Node, Double> weightsToProbabilities(@NonNull Map<Node, Double> children) {
        double countSum = children.values().stream().reduce(0D, Double::sum);

        return children.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / countSum));
    }

    @Override
    protected Optional<String> nodeColor(Node node) {
        return reachedTarget(node) ? Optional.of("red") : super.nodeColor(node);
    }
}
