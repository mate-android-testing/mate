package org.mate.exploration.rl.qlearning.autodroid;

import org.mate.MATE;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.ActionType;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Randomness;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates a new {@link IChromosome} or in the context of AutoDroid, a new episode is generated.
 */
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

    /**
     * Initialises the AutoDroid chromosome factory with the mandatory attributes.
     *
     * @param maxEpisodeLength The maximal episode length (max number of actions per test case).
     * @param initialQValue The initial q-value for a new action.
     * @param pHomeButton The probability for selecting the home button action.
     */
    public AutoDroidChromosomeFactory(int maxEpisodeLength, float initialQValue, float pHomeButton) {
        super(false, maxEpisodeLength);
        this.initialQValue = initialQValue;
        this.pHomeButton = pHomeButton;
    }

    /**
     * Creates a new chromosome (represents an episode in the context of AutoDroid). The chromosome
     * is filled with actions until either the maximal episode length is reached or an action closes
     * the AUT, either by a regular action or a crash.
     *
     * @return Returns the newly generated chromosome.
     */
    @Override
    public IChromosome<TestCase> createChromosome() {

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {

                IScreenState currentState = uiAbstractionLayer.getLastScreenState();

                Action nextAction = selectAction();
                MATELog.log_acc("Next action: " + nextAction);
                boolean leftApp = !testCase.updateTestCase(nextAction, actionsCount);

                if (leftApp) {
                    MATELog.log_acc("We left the app!");

                    /*
                    * If we directly select the home button action on a new state, the qValues map is
                    * not properly initialised and hence updating the map would lead to a crash.
                     */
                    if (qValues.containsKey(currentState)) {
                        qValues.get(currentState).put(nextAction, 0.0d);
                    }

                    return chromosome;
                }

                // compute reward of last action + update q-value
                double reward = computeReward(currentState, nextAction);
                IScreenState newState = uiAbstractionLayer.getLastScreenState();
                updateQValue(reward, nextAction, currentState, newState);
            }
        } finally {
            if (!isTestSuiteExecution) {
                /*
                 * If we deal with a test suite execution, the storing of coverage and fitness data
                 * is handled by the AndroidSuiteRandomChromosomeFactory itself.
                 */
                FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
                CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
                CoverageUtils.logChromosomeCoverage(chromosome);
            }
            testCase.finish();
        }
        return chromosome;
    }

    /**
     * Updates the q-value for the last executed action.
     *
     * @param reward The computed immediate reward for the given action.
     * @param action The last executed action.
     * @param oldState The state before executing the action.
     * @param newState The state after executing the action.
     */
    private void updateQValue(double reward, Action action, IScreenState oldState, IScreenState newState) {

        double discountFactor = computeDiscountFactor(newState);
        double futureReward = initialQValue;

        if (qValues.containsKey(newState)) {
            // pick the highest q-value as future reward
            futureReward = Collections.max(qValues.get(newState).values());
        }

        MATELog.log_acc("Discount factor: " + discountFactor);
        MATELog.log_acc("Intermediate reward: " + reward);
        MATELog.log_acc("Future reward: " + futureReward);

        double qValue = reward + discountFactor * futureReward;
        MATELog.log_acc("New q-value: " + qValue);
        qValues.get(oldState).put(action, qValue);
    }

    /**
     * Computes the dynamic discount factor.
     *
     * @param screenState The current screen state.
     * @return Returns the discount factor.
     */
    private double computeDiscountFactor(IScreenState screenState) {
        return 0.9 * Math.exp(-0.1*(screenState.getActions().size() - 1));
    }

    /**
     * Computes the reward for the last executed action.
     *
     * @param lastState The state before the action was executed.
     * @param lastAction The last executed action.
     * @return Returns the reward for the given action.
     */
    private double computeReward(IScreenState lastState, Action lastAction) {
        return (double) 1 / stateActionFrequencyMap.get(lastState).get(lastAction);
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

            // associate non-executed actions with the initial q-value and initialise execution counter
            Map<Action, Integer> actionFrequency = stateActionFrequencyMap.getOrDefault(currentState, new HashMap<>());
            Map<Action, Double> actionQValues = qValues.getOrDefault(currentState, new HashMap<>());

            if (!stateActionFrequencyMap.containsKey(currentState)) {
                MATELog.log_acc("Setting initial q-values and execution counter...");
                for (Action action : availableActions) {
                    actionFrequency.put(action, 0);
                    actionQValues.put(action, (double) initialQValue);
                }
                stateActionFrequencyMap.put(currentState, actionFrequency);
                qValues.put(currentState, actionQValues);
            }

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
