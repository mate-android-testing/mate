package org.mate.exploration.genetic.util.eda;

import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.ActionType;
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

/**
 * The (default) initialization strategy of weights/probabilities for each action of a state.
 */
public class ProbabilityInitialization implements BiFunction<List<Action>, IScreenState, Map<Action, Double>> {

    /**
     * An additional weight factor for promising actions.
     */
    private final double pPromisingAction;

    /**
     * Initialises the probability initializer with the given weight factor for promising actions.
     *
     * @param pPromisingAction The weight factor for promising actions, must be between 0 and 1.
     */
    public ProbabilityInitialization(double pPromisingAction) {
        this.pPromisingAction = pPromisingAction;
    }

    /**
     * Computes the probabilities for the actions of the given state.
     *
     * @param prevActions The list of actions that have been executed so far.
     * @param state The state for which the action probabilities should be computed.
     * @return Returns a map defining for each action the probability.
     */
    @Override
    public Map<Action, Double> apply(final List<Action> prevActions, final IScreenState state) {

        final Map<Action, Double> probabilities = new HashMap<>();
        final Set<Action> promisingActions
                = new HashSet<>(Registry.getUiAbstractionLayer().getPromisingActions(state));

        // The PIPE paper differentiates between terminals and functions, which is why they define
        // a probability P_T for picking a terminal.
        // We differentiate between promising actions and normal ones

        // P_j(I) = P_T / l, where I elem promising actions
        // P_j(I) = (1 - P_T) / l, where I not elem promising actions
        for (final Action action : state.getActions()) {
            double weight = getActionWeight(prevActions, state, action) *
                    (promisingActions.contains(action) ? pPromisingAction : (1 - pPromisingAction));
            probabilities.put(action, weight);
        }

        return toProbabilities(probabilities);
    }

    /**
     * Converts the action weights to probabilities. The sum over the probabilities must be 1.
     *
     * @param weights The map of action weights.
     * @return Returns the probability for each action.
     */
    private Map<Action, Double> toProbabilities(final Map<Action, Double> weights) {

        final Map<Action, Double> probabilities = new HashMap<>();

        double sum = weights.values().stream().mapToDouble(Number::doubleValue).sum();

        // Assign to each action a probability proportionate to its weight.
        for (Map.Entry<Action, Double> weightEntry : weights.entrySet()) {
            probabilities.put(weightEntry.getKey(), weightEntry.getValue() / sum);
        }

        return probabilities;
    }

    // TODO: Remove.
    private <T> Map<T, Double> fixed(final Map<T, Double> weights, final double maxProb) {

        final Map<T, Double> probabilities = new HashMap<>();
        final Queue<Set<T>> sortedEntries = getFronts(weights);

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

    // TODO: Remove.
    private <T> Queue<Set<T>> getFronts(final Map<T, Double> weights) {

        final Map<Double, Set<T>> inverted = new HashMap<>();
        weights.forEach((key, value) -> inverted.computeIfAbsent(value, a -> new HashSet<>()).add(key));

        final ToDoubleFunction<Map.Entry<Double, Set<T>>> toDoubleFunction = Map.Entry::getKey;

        return inverted.entrySet().stream()
                .sorted(Comparator.comparingDouble(toDoubleFunction).reversed())
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Computes the action weight for the given action.
     *
     * @param prevActions The list of previously executed actions.
     * @param state The current state.
     * @param action The action for which the action weight should be computed.
     * @return Returns the action weight for the given action.
     */
    private double getActionWeight(final List<Action> prevActions, final IScreenState state,
                                   final Action action) {

        // the weight depends on the action type
        double eventTypeWeight = actionLeavesAUT(state, action) ? 0.1 : getActionTypeWeight(action);

        // TODO: Here is some computation missing.
        int unvisitedChildren = 0;

        // count how often the given action has been executed so far (+1 to avoid division by zero)
        long executionFrequency = prevActions.stream().filter(a -> a.equals(action)).count() + 1;

        double alpha = 1;
        double beta = 0.3;
        double gamma = 1.5;
        return ((alpha * eventTypeWeight) + (beta * unvisitedChildren)) / (gamma * executionFrequency);
    }

    /**
     * Checks whether the given action would leave the AUT when applied in the given state.
     *
     * @param state The given state.
     * @param action The given action.
     * @return Returns {@code true} if the action leaves the AUT, otherwise {@code false} is returned.
     */
    private boolean actionLeavesAUT(final IScreenState state, final Action action) {
        final IGUIModel iguiModel = Registry.getUiAbstractionLayer().getGuiModel();
        return iguiModel.getEdges(action).stream()
                .anyMatch(edge -> edge.getSource().equals(state)
                        && !edge.getTarget().getPackageName().equals(Registry.getPackageName())
                );
    }

    /**
     * Computes the action type weight for the given action. Certain actions should be favored over
     * other actions, e.g. a click is favored over an dpad action.
     *
     * @param action The given action.
     * @return Returns the action type weight for the given action.
     */
    private double getActionTypeWeight(final Action action) {

        if (action instanceof UIAction) {

            final ActionType actionType = ((UIAction) action).getActionType();

            switch (actionType) {
                case DPAD_UP:
                case DPAD_DOWN:
                case DPAD_LEFT:
                case DPAD_RIGHT:
                case DPAD_CENTER:
                    return 0.1;
                case SWIPE_UP:
                case SWIPE_LEFT:
                case SWIPE_RIGHT:
                case DELETE:
                case SEARCH:
                    return 0.5;
                case MENU:
                case FILL_FORM_AND_SUBMIT:
                case SWIPE_DOWN:
                    return 2;
                case CLICK:
                    return 1.5;
                default:
                    return 1;
            }
        } else {
            // TODO: Differentiate between intent and system actions.
            return 1;
        }
    }
}
