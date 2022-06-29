package org.mate.crash_reproduction.eda.util;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.model.Edge;
import org.mate.state.IScreenState;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StateActionTree<Weight extends Number> {
    private final BiFunction<IScreenState, Action, Weight> defaultWeightFunction;
    private final Weight minWeight;
    private final Map<Action, IScreenState> actionToNextState = new HashMap<>();
    private final List<Map<Action, Weight>> stateActionTree = new LinkedList<>();

    public StateActionTree(BiFunction<IScreenState, Action, Weight> defaultWeightFunction, Weight minWeight) {
        this.defaultWeightFunction = defaultWeightFunction;
        this.minWeight = minWeight;
    }

    public void clearWeights() {
        for (Map<Action, Weight> weights : stateActionTree) {
            weights.clear();
        }
    }

    public void updateWeightOfAction(int index, Action action, Function<Weight, Weight> weightUpdate) {
        getWeightsForIndex(index).compute(action, (a, oldValue) -> weightUpdate.apply(oldValue));
    }

    public void updateActionTarget(Action action, IScreenState targetState) {
        actionToNextState.put(action, targetState);
    }

    public Map<Action, Double> getActionProbabilitiesForState(int index, IScreenState state) {
        return weightsToProbabilities(getActionWeightsForState(index, state));
    }

    private Map<Action, Weight> getActionWeightsForState(int index, IScreenState state) {
        Set<Action> possibleActions = new HashSet<>(state.getActions());
        Map<Action, Weight> weights = getWeightsForIndex(index);

        for (Action possibleAction : possibleActions) {
            if (!weights.containsKey(possibleAction)) {
                weights.put(possibleAction, defaultWeightFunction.apply(state, possibleAction));
            }
        }

        return getWeightsForIndex(index).entrySet().stream()
                .filter(e -> possibleActions.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Action, Double> weightsToProbabilities(Map<Action, Weight> weights) {
        double sum = weights.values().stream().mapToDouble(Number::doubleValue)
                .map(v -> Math.max(minWeight.doubleValue(), v))
                .sum();

        return weights.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, frequency -> Math.max(frequency.getValue().doubleValue(), minWeight.doubleValue()) / sum));
    }

    private Map<Action, Weight> getWeightsForIndex(int index) {
        while (index >= stateActionTree.size()) {
            stateActionTree.add(new HashMap<>());
        }
        return stateActionTree.get(index);
    }

    private List<Edge> bestPath() {
        List<Edge> path = new LinkedList<>();
        IScreenState currentState = Registry.getUiAbstractionLayer().getGuiModel().getRootState();

        for (int i = 0; i < Properties.MAX_NUMBER_EVENTS() && currentState != null; i++) {
            Action bestAction = getActionProbabilitiesForState(i, currentState).entrySet().stream()
                    .max(Comparator.comparingDouble(Map.Entry::getValue)).map(Map.Entry::getKey).orElseThrow(IllegalStateException::new);

            IScreenState nextState = actionToNextState.get(bestAction);
            if (nextState != null) {
                path.add(new Edge(bestAction, currentState, nextState));
            }
            currentState = nextState;
        }

        return path;
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner("\n");
        stringJoiner.add("digraph D {");
        Set<IScreenState> states = new HashSet<>();
        states.add(Registry.getUiAbstractionLayer().getGuiModel().getRootState());
        BiFunction<Integer, IScreenState, String> stateLabel = (level, state) -> '"' + "Level " + level + " " + state.getId() + '"';
        List<Edge> bestPath = bestPath();

        for (int i = 0; i < Properties.MAX_NUMBER_EVENTS(); i++) {
            Optional<Edge> bestEdge = i < bestPath.size() ? Optional.of(bestPath.get(i)) : Optional.empty();
            Set<IScreenState> statesAtNextLevel = new HashSet<>();

            for (IScreenState state : states) {
                List<Map.Entry<Action, Double>> unknownTargetState = new LinkedList<>();
                Map<Action, Double> probabilities = getActionProbabilitiesForState(i, state);
                Map<IScreenState, Set<Map.Entry<Action, Double>>> stateToIncomingEdges = new HashMap<>();

                for (Map.Entry<Action, Double> actionWithProbability : probabilities.entrySet()) {
                    IScreenState target = actionToNextState.get(actionWithProbability.getKey());

                    if (target == null) {
                        unknownTargetState.add(actionWithProbability);
                    } else {
                        statesAtNextLevel.add(target);
                        stateToIncomingEdges.computeIfAbsent(target, t -> new HashSet<>()).add(actionWithProbability);
                    }
                }

                for (Map.Entry<IScreenState, Set<Map.Entry<Action, Double>>> edges : stateToIncomingEdges.entrySet()) {
                    // Merge all edges into one
                    stringJoiner.add(String.format(Locale.getDefault(), "%s -> %s [label=<%s>, color=%s]",
                            stateLabel.apply(i, state),
                            stateLabel.apply(i + 1, edges.getKey()),
                            edges.getValue().stream().map(actionWithProb -> {
                                String label = actionWithProb.getKey().toShortString() + ": " + actionWithProb.getValue();

                                if (bestEdge.isPresent() && bestEdge.get().getSource().equals(state) && bestEdge.get().getTarget().equals(edges.getKey()) && bestEdge.get().getAction().equals(actionWithProb.getKey())) {
                                    label = "<B>" + label + "</B>";
                                }
                                return label;
                            }).collect(Collectors.joining("<BR/>")),
                            bestEdge.isPresent() && bestEdge.get().getSource().equals(state) && bestEdge.get().getTarget().equals(edges.getKey()) && edges.getValue().stream().anyMatch(a -> bestEdge.get().getAction().equals(a.getKey())) ? "red" : "black"
                    ));
                }

                stringJoiner.add(String.format(Locale.getDefault(),
                        "%s [xlabel=<%s>, image=\"results/pictures/%s/%s.png\", imagescale=true, height=6, fixedsize=true, shape=square%s]",
                        stateLabel.apply(i, state),
                        unknownTargetState.stream().map(e -> e.getKey().toShortString() + ": " + e.getValue())
                                .collect(Collectors.joining("<BR/>")),
                        Registry.getPackageName(),
                        state.getId(),
                        bestEdge.isPresent() && state.equals(bestEdge.get().getSource()) ? ", fillcolor=red, style=filled" : ""
                ));
            }

            states = statesAtNextLevel;
        }

        stringJoiner.add("}");

        return stringJoiner.toString();
    }
}
