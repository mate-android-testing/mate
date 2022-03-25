package org.mate.exploration.rl.qlearning.autoblacktest;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.Widget;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AutoBlackTestChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * The epsilon used in the epsilon-greedy learning policy.
     */
    private final float epsilon;

    /**
     * The static discount factor gamma used in equation (1).
     */
    private final float discountFactor;

    /**
     * Maintains the q-values for each state and action.
     */
    private final Map<IScreenState, Map<Action, Double>> qValues = new HashMap<>();

    public AutoBlackTestChromosomeFactory(int maxEpisodeLength, float epsilon, float discountFactor) {
        super(false, maxEpisodeLength);
        this.epsilon = epsilon;
        this.discountFactor = discountFactor;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {

                IScreenState oldState = uiAbstractionLayer.getLastScreenState();

                Action nextAction = selectAction();
                MATE.log_acc("Next action: " + nextAction);
                boolean leftApp = !testCase.updateTestCase(nextAction, actionsCount);

                // compute reward of last action + update q-value
                IScreenState newState = uiAbstractionLayer.getLastScreenState();
                double reward = computeReward(oldState, newState, nextAction);
                updateQValue(reward, oldState, newState);

                if (leftApp) {
                    return chromosome;
                }
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

    private double computeReward(IScreenState oldState, IScreenState newState, Action action) {
        return 0.0;
    }

    /**
     * Computes the state difference in terms of the number of widgets that only appear in the first
     * state but not in the second state according to its traits. See the restriction operator \t
     * defined on page 83 in the paper.
     *
     * @param firstState The first state.
     * @param secondState The second state.
     * @return Returns the number of widgets that only appear in the first state but not in the second.
     */
    private int stateDifference(IScreenState firstState, IScreenState secondState) {

        // if the states are equal according to our global equality check -> diff close to 0 ???
        if (firstState.equals(secondState)) {
            MATE.log_acc("Comparing same states!");
        }

        // the widgets that only appear in the first and not in the second state according to its traits.
        Set<Widget> widgets = new HashSet<>();

        for (Widget thisWidget : firstState.getWidgets()) {

            WidgetTrait thisTrait = new WidgetTrait(thisWidget);
            boolean notContainedInSecondState = true;

            for (Widget otherWidget : secondState.getWidgets()) {

                WidgetTrait otherTrait = new WidgetTrait(otherWidget);

                if (thisTrait.equals(otherTrait)) {
                    notContainedInSecondState = false;
                    break;
                }
            }

            if (notContainedInSecondState) {
                widgets.add(thisWidget);
            }
        }

        MATE.log_acc("Number of widgets in set difference: " + widgets.size());
        return widgets.size();
    }

    private void updateQValue(double reward, IScreenState oldState, IScreenState newState) {

    }

    /**
     * Selects the action that should be executed next. We choose with a probability of epsilon
     * a random action and with a of probability 1 - epsilon the action with the highest q-value.
     *
     * @return Returns the action that should be executed next.
     */
    @Override
    protected Action selectAction() {

        IScreenState lastScreenState = uiAbstractionLayer.getLastScreenState();
        double rnd = Randomness.getRnd().nextDouble();

        if (rnd < epsilon) {
            // select randomly with probability epsilon
            MATE.log_acc("Selecting random action!");
            return Randomness.randomElement(lastScreenState.getActions());
        } else {
            // select the action with the highest q-value with probability 1 - epsilon
            MATE.log_acc("Selecting action with highest q-value!");

            // init q-values for new state
            if (!qValues.containsKey(lastScreenState)) {
                MATE.log_acc("New state: " + lastScreenState);
                Map<Action, Double> actionQValueMapping = new HashMap<>();
                for (Action action : lastScreenState.getActions()) {
                    actionQValueMapping.put(action, 0.0d);
                }
                this.qValues.put(lastScreenState, actionQValueMapping);
            }

            // select an action associated with the highest q-value
            Map<Action, Double> actionQValueMapping = qValues.get(lastScreenState);
            double maxQValue = Collections.max(actionQValueMapping.values());
            List<Action> highestQValueActions = actionQValueMapping.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxQValue)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            return Randomness.randomElement(highestQValueActions);
        }
    }
}
