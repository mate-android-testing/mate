package org.mate.crash_reproduction.eda;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hamcrest.core.Is;
import org.mate.MATE;
import org.mate.Registry;
import org.mate.crash_reproduction.CrashReproduction;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.exploration.genetic.fitness.CallTreeDistance;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.Edge;
import org.mate.model.IGUIModel;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.Randomness;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GUIModelDistributionModel<Node extends UIAction> implements IDistributionModel<Node> {
    private final IFitnessFunction<TestCase> fitnessFunction = new CallTreeDistance<>();
    private final Set<String> targets = Registry.getEnvironmentManager().getTargetActivities();
    private final IGUIModel guiModel;
    private final Map<Edge, Double> weights = new HashMap<>();
    private final Set<IScreenState> seenStates = new HashSet<>();
    private final Set<Edge> seenEdges = new HashSet<>();

    public GUIModelDistributionModel(IGUIModel guiModel) {
        this.guiModel = guiModel;
    }

    @Override
    public void update(Set<IChromosome<TestCase>> node) {
        for (IChromosome<TestCase> testCaseIChromosome : node) {
            TestCase testCase = testCaseIChromosome.getValue();
            double fitness = fitnessFunction.getFitness(testCaseIChromosome);
            double weightFactor = fitnessFunction.isMaximizing() ? fitness : (1 - fitness);

            for (int i = 0; i < testCase.getEventSequence().size() && i < testCase.getStateSequence().size() - 1; i++) {
                Action action = testCase.getEventSequence().get(i);
                IScreenState before = testCase.getStateSequence().get(i);
                IScreenState after = testCase.getStateSequence().get(i + 1);

                Edge edge = getEdge(before, after, action);

                weights.compute(edge, (e, prevValue) -> {
                   prevValue = prevValue == null ? 0 : prevValue;

                   return (prevValue + getWeightUpdateValue(edge)) * weightFactor;
                });
            }
        }

        Map<Stream<Edge>, Double> booster = new HashMap<>();
        booster.put(getPathsToTarget().stream().flatMap(Collection::stream), 5D);
        booster.put(getPathsToPromisingActionsOnTargets().stream().flatMap(Collection::stream), 3D);
        booster.put(getPromisingEdges(), 2D);

        booster.forEach((edges, weightUpdate) -> edges.distinct().forEach(edge -> weights.compute(edge, (e, prevValue) -> {
            prevValue = prevValue == null ? 0 : prevValue;

            return prevValue + weightUpdate;
        })));

        // Forget edges leading to boring states (that only lead back to parent)
        Set<Edge> boringEdges = weights.keySet().stream()
                .filter(this::isBoringEdge)
                .peek(e -> MATE.log("Removing boring edge from model: " + e))
                .collect(Collectors.toSet());

        for (Edge edge : boringEdges) {
            weights.remove(edge);
        }
    }

    private Stream<Edge> getIncomingEdges(IScreenState state) {
        return weights.keySet().stream().filter(e -> e.getTarget().equals(state));
    }

    private Stream<Edge> getOutgoingEdges(IScreenState state) {
        return weights.keySet().stream().filter(e -> e.getSource().equals(state));
    }

    private boolean isBoringEdge(Edge edge) {
        Optional<Edge> nextBestEdge = getNextBestEdge(edge.getTarget());

        // Going in circles
        return nextBestEdge.isPresent()
                && !edge.getSource().equals(edge.getTarget())
                && nextBestEdge.get().getTarget().equals(edge.getSource());
    }

    private Set<List<Edge>> getPathsToTarget() {
        return getPathsTo(this::reachedTarget);
    }

    private Set<List<Edge>> getPathsToPromisingActionsOnTargets() {
        return getPathsTo(s -> reachedTarget(s) && containsPromisingActions(s));
    }

    private Stream<Edge> getPromisingEdges() {
        return weights.keySet().stream()
                .filter(this::isPromisingEdge);
    }

    private Set<List<Edge>> getPathsTo(Predicate<IScreenState> isTarget) {
        IScreenState root = guiModel.getRootState();
        return getAllStates().stream()
                .filter(isTarget)
                .map(target -> guiModel.shortestPath(root, target))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private Edge getEdge(IScreenState state1, IScreenState state2, Action action) {
        return new Edge(action, state1, state2);
    }

    private boolean isPromisingEdge(Edge edge) {
        return Registry.getUiAbstractionLayer().getPromisingActions(edge.getSource()).contains(edge.getAction());
    }

    private double getWeightUpdateValue(Edge edge) {
        seenStates.add(edge.getSource());
        boolean executedNewPromisingAction = seenEdges.add(edge) && isPromisingEdge(edge);
        boolean discoveredNewState = seenStates.add(edge.getTarget());
        boolean startAtTarget = reachedTarget(edge.getSource());
        boolean endAtTarget = reachedTarget(edge.getTarget());

        if (startAtTarget) {
            if (endAtTarget) {
                // Stayed on target
                return 3 + (executedNewPromisingAction ? 2 : 0) + (discoveredNewState ? 2 : 0);
            } else {
                // Left target
                return 0.1;
            }
        } else {
            if (endAtTarget) {
                // Discovered target
                return 10;
            } else {
                // Boring
                return 1 + (executedNewPromisingAction ? 1 : 0) + (discoveredNewState ? 1 : 0);
            }
        }
    }

    @Override
    public Optional<Node> drawNextNode(UIAction startNode) {
        Map<Edge, Double> probabilities = getProbabilities();
        if (probabilities.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of((Node) Randomness.randomIndexWithProbabilities(probabilities).getAction());
        }
    }

    @Override
    public Optional<Node> getNextBestNode(UIAction startNode) {
        return getNextBestEdge(Registry.getUiAbstractionLayer().getLastScreenState()).map(e -> (Node) e.getAction());
    }

    private Optional<Edge> getNextBestEdge(IScreenState node) {
        return getProbabilities(node).entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey);
    }

    @Override
    public Set<Node> getPossibleNodes(Node startNode) {
        return guiModel.getOutgoingEdges(Registry.getUiAbstractionLayer().getLastScreenState()).stream().map(e -> (Node) e.getAction()).collect(Collectors.toSet());
    }

    public List<Edge> getBestSequence(IScreenState start) {
        start = start == null ? guiModel.getRootState() : start;
        List<Edge> bestPath = new LinkedList<>();

        Optional<Edge> nextBest = getNextBestEdge(start);
        while (nextBest.isPresent()) {
            Edge nextBestNode = nextBest.get();

            if (bestPath.contains(nextBestNode)) {
                break;
            } else {
                bestPath.add(nextBestNode);
                nextBest = getNextBestEdge(nextBestNode.getTarget());
            }
        }

        return bestPath;
    }

    private Map<Edge, Double> getProbabilities() {
        return getProbabilities(Registry.getUiAbstractionLayer().getLastScreenState());
    }

    private Map<Edge, Double> getProbabilities(IScreenState state) {
        Set<Edge> edges = guiModel.getOutgoingEdges(state);
        double totalWeight = 0;
        for (Edge edge : edges) {
            totalWeight += weights.getOrDefault(edge, 0D);
        }

        Map<Edge, Double> probabilities = new HashMap<>();
        for (Edge edge : edges) {
            double weight = weights.getOrDefault(edge, 0D);
            if (weight > 0) {
                probabilities.put(edge, weight / totalWeight);
            }
        }
        MATE.log("ProbSum: " + probabilities.values().stream().mapToDouble(d -> d).sum());
        return probabilities;
    }

    private String dumpGraph() {
        StringJoiner graph = new StringJoiner("\n");
        graph.add("digraph D {");
        List<Edge> bestSequence = getBestSequence(guiModel.getRootState());
        MATE.log("Best sequence length: " + bestSequence.size());

        Set<IScreenState> allNodes = getAllStates();
        for (IScreenState node : allNodes) {

            Map<Edge, Double> children = mergeEdges(getProbabilities(node), 3);
            MATE.log("#GroupDebug Group size after merge: " + children.size());

            children.forEach((e, v) -> {
                graph.add(String.format(Locale.getDefault(),
                        "\"%s\" -> \"%s\" [ label=\"%s abs: %.1f, rel: %.3f\", color=%s ];",
                        e.getSource().toString() + e.getSource().hashCode(),
                        e.getTarget().toString() + e.getTarget().hashCode(),
                        e.getAction().toShortString(),
                        weights.get(e),
                        v,
                        bestSequence.contains(e) ? "red" : "black"
                        )
                );
            });
        }

        for (IScreenState state : allNodes) {
            List<String> attributes = new LinkedList<>();

            attributes.add("image=\"results/pictures/" + Registry.getPackageName() + "/" + state.getId() + ".png\"");
            attributes.add("imagescale=true, height=6, fixedsize=true, shape=square");

            boolean containsPromisingActions = containsPromisingActions(state);
            boolean reachedTarget = reachedTarget(state);

            Optional<String> color = Optional.empty();

            if (containsPromisingActions && reachedTarget) {
                color = Optional.of("purple");
            } else if (containsPromisingActions) {
                color = Optional.of("blue");
            } else if (reachedTarget) {
                color = Optional.of("red");
            }

            color.ifPresent(c -> attributes.add("fillcolor=" + c + ", style=filled"));

            if (!attributes.isEmpty()) {
                graph.add(String.format("\"%s\" [%s]", state.toString() + state.hashCode(), attributes.stream().collect(Collectors.joining(", "))));
            }
        }

        graph.add("}");

        return graph.toString();
    }

    private Map<Edge, Double> mergeEdges(Map<Edge, Double> probabilities, int keepBestX) {
        Map<Edge, Double> edges = new HashMap<>();
        ToDoubleFunction<Map.Entry<Edge, Double>> probGetter = e -> e.getValue();

        for (Map<Edge, Double> edgeDirection : groupBySourceAndTarget(probabilities)) {
            Queue<Map.Entry<Edge, Double>> sortedEdges = edgeDirection.entrySet().stream()
                    .sorted(Comparator.comparingDouble(probGetter).reversed())
                    .collect(Collectors.toCollection((LinkedList::new)));

            int took = 0;
            while (!sortedEdges.isEmpty() && took < keepBestX) {
                Map.Entry<Edge, Double> entry = sortedEdges.poll();
                edges.put(entry.getKey(), entry.getValue());
                took++;
            }
            MATE.log("#GroupDebug Group Took " + took);

            if (!sortedEdges.isEmpty()) {
                Map.Entry<Edge, Double> exampleEdge = sortedEdges.poll();
                Action dummyAction = sortedEdges.isEmpty() ? exampleEdge.getKey().getAction() : new Action() {
                    @NonNull
                    @Override
                    public String toString() {
                        return toShortString();
                    }

                    @NonNull
                    @Override
                    public String toShortString() {
                        return "Summary Action";
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }

                    @Override
                    public boolean equals(@Nullable Object o) {
                        return o == this;
                    }
                };
                Edge edge = new Edge(dummyAction, exampleEdge.getKey().getSource(), exampleEdge.getKey().getTarget());
                double probabilitySum = exampleEdge.getValue() + sortedEdges.stream().mapToDouble(Map.Entry::getValue).sum();
                edges.put(edge, probabilitySum);
            }
        }

        return edges;
    }

    private Collection<Map<Edge, Double>> groupBySourceAndTarget(Map<Edge, Double> probabilities) {
        Map<Integer, Map<Edge, Double>> groups = new HashMap<>();

        probabilities.forEach((edge, probability) -> groups.computeIfAbsent(Objects.hash(edge.getSource(), edge.getTarget()), k -> new HashMap<>())
                .put(edge, probability));
        MATE.log("#GroupDebug #Groups " + groups.size());

        return groups.values();
    }

    @Override
    public String toString() {
        return dumpGraph();
    }

    private boolean reachedTarget(IScreenState state) {
        return CrashReproduction.reachedTarget(targets, state);
    }

    private Set<IScreenState> getAllStates() {
        return weights.keySet().stream().flatMap(e -> Stream.of(e.getSource(), e.getTarget())).collect(Collectors.toSet());
    }

    private boolean containsPromisingActions(IScreenState state) {
        return !Registry.getUiAbstractionLayer().getPromisingActions(state).isEmpty();
    }
}
