package org.mate.crash_reproduction.eda.representation.initializer;

import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.UIAction;
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
            double weight = getActionWeight(prevActions, (UIAction) action) *
                    (promisingActions.contains(action) ? pPromisingAction : (1 - pPromisingAction));
            probabilities.put(action, weight);
        }

        return fixed(probabilities, 0.7);
    }

    private <T> Map<T, Double> fixed(Map<T, Double> weights, double maxProb) {
        ToDoubleFunction<Map.Entry<T, Double>> toDoubleFunction = Map.Entry::getValue;

        Map<T, Double> probabilities = new HashMap<>();
        Queue<T> sortedEntries = weights.entrySet().stream()
                .sorted(Comparator.comparingDouble(toDoubleFunction).reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedList::new));

        double leftToGive = 1;
        while (!sortedEntries.isEmpty()) {
            double prob = leftToGive * maxProb;
            probabilities.put(sortedEntries.poll(), prob);

            leftToGive -= prob;
        }

        return probabilities;
    }

    private double getActionWeight(List<Action> prevActions, UIAction action) {
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
                eventTypeWeight = 0.5;
                break;
            case MENU:
                eventTypeWeight = 2;
                break;
            default:
                eventTypeWeight = 1;
                break;
        }
        if (action.getActionType() == ActionType.BACK && action.getActivityName().equals(Registry.getMainActivity())) {
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
}
