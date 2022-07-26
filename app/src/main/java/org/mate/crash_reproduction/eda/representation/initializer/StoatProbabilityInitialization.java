package org.mate.crash_reproduction.eda.representation.initializer;

import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class StoatProbabilityInitialization implements BiFunction<List<Action>, IScreenState, Map<Action, Double>> {
    private final double pPromisingAction;

    public StoatProbabilityInitialization(double pPromisingAction) {
        this.pPromisingAction = pPromisingAction;
    }

    @Override
    public Map<Action, Double> apply(List<Action> prevActions, IScreenState state) {
        Map<Action, Double> probabilities = new HashMap<>();
        Set<Action> promisingActions = new HashSet<>(Registry.getUiAbstractionLayer().getPromisingActions(state));

        // The PIPE paper differentiates between terminals and functions, which is why they define
        // a probability P_T for picking a terminal.
        // We differentiate between promising actions and normal ones

        // P_j(I) = P_T / l, where I elem promising actions
        // P_j(I) = (1 - P_T) / l, where I not elem promising actions
        for (Action action : state.getActions()) {
            double weight = getActionWeight(prevActions, state, (UIAction) action) *
                    (promisingActions.contains(action) ? pPromisingAction : (1 - pPromisingAction));
            probabilities.put(action, weight);
        }

        return fixed(probabilities, 0.5);
    }

    private <T> Map<T, Double> fixed(Map<T, Double> weights, double maxProb) {
        Map<T, Double> probabilities = new HashMap<>();
        Queue<Set<T>> sortedEntries = getFronts(weights);

        double leftToGive = 1;
        while (!sortedEntries.isEmpty()) {
            Set<T> entries = sortedEntries.poll();

            double prob = 0;

            for (int i = 0; i < entries.size(); i++) {
                double p = leftToGive * maxProb;
                leftToGive -= p;
                prob += p;
            }

            double probPerEntry = prob / entries.size();
            for (T entry : entries) {
                probabilities.put(entry, probPerEntry);
            }
        }

        return probabilities;
    }

    private <T> Queue<Set<T>> getFronts(Map<T, Double> weights) {
        Map<Double, Set<T>> inverted = new HashMap<>();
        weights.forEach((key, value) -> inverted.computeIfAbsent(value, a -> new HashSet<>()).add(key));

        ToDoubleFunction<Map.Entry<Double, Set<T>>> toDoubleFunction = Map.Entry::getKey;

        return inverted.entrySet().stream()
                .sorted(Comparator.comparingDouble(toDoubleFunction).reversed())
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private double getActionWeight(List<Action> prevActions, IScreenState state, UIAction action) {
        // the weight depends on the action type
        double eventTypeWeight;
        switch (action.getActionType()) {
            case DPAD_UP:
            case DPAD_DOWN:
            case DPAD_LEFT:
            case DPAD_RIGHT:
            case DPAD_CENTER:
                eventTypeWeight = 0.1;
                break;
            case SWIPE_UP:
            case SWIPE_DOWN:
            case SWIPE_LEFT:
            case SWIPE_RIGHT:
            case DELETE:
            case SEARCH:
                eventTypeWeight = 0.5;
                break;
            case MENU:
                eventTypeWeight = 2;
                break;
            case CLICK:
                eventTypeWeight = 1.5;
                break;
            default:
                eventTypeWeight = 1;
                break;
        }
        if (actionForcesTestToFinish(state, action)) {
            eventTypeWeight = 0.1;
        }

        int unvisitedChildren = 0;

        // add 1 to not divide by zero
        long executionFrequency = prevActions.stream().filter(a -> a.equals(action)).count() + 1;

        double alpha = 1;
        double beta = 0.3;
        double gamma = 1.5;
        return ((alpha * eventTypeWeight) + (beta * unvisitedChildren)) / (gamma * executionFrequency);
    }

    private boolean actionForcesTestToFinish(IScreenState state, Action action) {
        IGUIModel iguiModel = Registry.getUiAbstractionLayer().getGuiModel();
        return iguiModel.getEdges(action).stream()
                .anyMatch(edge -> edge.getSource().equals(state)
                        && !edge.getTarget().getPackageName().equals(Registry.getPackageName()) // action leads outside the app
                );
    }
}
