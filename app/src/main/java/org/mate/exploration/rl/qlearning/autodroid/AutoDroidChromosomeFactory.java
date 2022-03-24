package org.mate.exploration.rl.qlearning.autodroid;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AutoDroidChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * The initial q-value for a new action.
     */
    private final float initialQValue;

    /**
     * Maintains for each state how often a particular has been executed so far.
     */
    private final Map<IScreenState, Map<Action, Integer>> stateActionFrequencyMap = new HashMap<>();

    /**
     * Maintains the q-values for each state and action.
     */
    private final Map<IScreenState, Map<Action, Double>> qValues = new HashMap<>();

    /**
     * The probability for selecting the home button as next action.
     */
    private final float pHomeButton;

    public AutoDroidChromosomeFactory(int maxEpisodeLength, float initialQValue, float pHomeButton) {
        super(false, maxEpisodeLength);
        this.initialQValue = initialQValue;
        this.pHomeButton = pHomeButton;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        return super.createChromosome();
    }

    /**
     * Selects the next action to be executed. Picks a random probability of {@link #pHomeButton}
     * the 'HOME' action or otherwise an arbitrary action associated with the highest q-value on
     * the current state.
     *
     * @return Returns the selected action.
     */
    @Override
    protected Action selectAction() {

        double rnd = Randomness.getRnd().nextDouble();

        if (rnd < pHomeButton) {
            return new UIAction(ActionType.HOME, uiAbstractionLayer.getCurrentActivity());
        } else {

            IScreenState currentState = uiAbstractionLayer.getLastScreenState();
            List<UIAction> availableActions = currentState.getActions();

            // set the execution counter to 0 for new actions
            if (!stateActionFrequencyMap.containsKey(currentState)) {
                Map<Action, Integer> actionFrequency = new HashMap<>();
                for (Action action : availableActions) {
                    actionFrequency.put(action, 0);
                }
                stateActionFrequencyMap.put(currentState, actionFrequency);
            }

            // associate non-executed actions with the initial q-value
            Map<Action, Integer> actionFrequency = stateActionFrequencyMap.get(currentState);
            Map<Action, Double> actionQValues = qValues.getOrDefault(currentState, new HashMap<>());

            for (Action action : availableActions) {
                int executionCount = actionFrequency.get(action);
                if (executionCount == 0) {
                    actionQValues.put(action, (double) initialQValue);
                }
            }

            qValues.put(currentState, actionQValues);

            // select an action associated with the highest q-value
            double maxQValue = Collections.max(actionQValues.values());
            List<Action> highestQValueActions = actionQValues.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxQValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            Action nextAction = Randomness.randomElement(highestQValueActions);

            // update the execution counter
            actionFrequency.computeIfPresent(nextAction, (action, ctr) -> ctr + 1);

            return nextAction;
        }
    }
}
