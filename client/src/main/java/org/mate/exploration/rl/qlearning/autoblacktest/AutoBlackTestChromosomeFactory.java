package org.mate.exploration.rl.qlearning.autoblacktest;

import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.commons.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates a new {@link IChromosome} or in the context of AutoBlackTest, a new episode is generated.
 */
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

    /**
     * Initialises the AutoBlackTest chromosome factory with the mandatory attributes.
     *
     * @param maxEpisodeLength The maximal episode length (maximal number of actions per test case).
     * @param epsilon The epsilon used in the epsilon-greedy action selection strategy.
     * @param discountFactor The discount factor gamma used in the q-learning formula.
     */
    public AutoBlackTestChromosomeFactory(int maxEpisodeLength, float epsilon, float discountFactor) {
        super(false, maxEpisodeLength);
        this.epsilon = epsilon;
        this.discountFactor = discountFactor;
    }

    /**
     * Creates a new chromosome (represents an episode in the context of AutoBlackTest). The chromosome
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

                IScreenState oldState = uiAbstractionLayer.getLastScreenState();
                checkForNewState(oldState);

                Action nextAction = selectAction();
                MATELog.log_acc("Next action: " + nextAction);
                boolean leftApp = !testCase.updateTestCase(nextAction, actionsCount);

                // compute reward of last action + update q-value
                IScreenState newState = uiAbstractionLayer.getLastScreenState();
                double reward = computeReward(oldState, newState);
                updateQValue(reward, oldState, newState, nextAction);

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

    /**
     * Computes the intermediate reward for the last action, see the definition on page 84.
     *
     * @param oldState The state before executing the last action.
     * @param newState The state after executing the last action.
     * @return Returns the intermediate reward of the last action.
     */
    private double computeReward(IScreenState oldState, IScreenState newState) {

        // |AS 2 \t AS 1|
        int stateDifference = stateDifference(newState, oldState);

        // ∑ w1∈AS 1, w2∈AS 2, w1 =t w2 diff(w1,w2)
        double widgetDifferences = 0;

        for (Widget thisWidget : oldState.getWidgets()) {
            for (Widget otherWidget : newState.getWidgets()) {

                WidgetTrait thisTrait = new WidgetTrait(thisWidget);
                WidgetTrait otherTrait = new WidgetTrait(otherWidget);

                if (thisTrait.equals(otherTrait)) {
                    widgetDifferences += widgetDifference(thisWidget, otherWidget);
                }
            }
        }

        return (double) (stateDifference + widgetDifferences) / newState.getWidgets().size();
    }

    /**
     * Computes the difference of two widgets in terms of the changed property values, see the
     * equation on page 84.
     *
     * @param firstWidget The first widget.
     * @param secondWidget The second widget.
     * @return Returns the difference of two widgets in terms of changed property values.
     */
    private double widgetDifference(Widget firstWidget, Widget secondWidget) {

        // diff(w1,w2) = |P1\P2| + |P2\P1| / |P1| + |P2|
        int widgetDifferences = 0;

        if (firstWidget.isEnabled() != secondWidget.isEnabled()) {
            widgetDifferences++;
        }

        if (firstWidget.isVisible() != secondWidget.isVisible()) {
            widgetDifferences++;
        }

        if (firstWidget.isEditable() != secondWidget.isEditable()) {
            widgetDifferences++;
        }

        if (firstWidget.isChecked() != secondWidget.isChecked()) {
            widgetDifferences++;
        }

        if (firstWidget.isFocused() != secondWidget.isFocused()) {
            widgetDifferences++;
        }

        if (!firstWidget.getText().equals(secondWidget.getText())) {
            widgetDifferences++;
        }

        return (double) widgetDifferences / 6 + 6;
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

        return widgets.size();
    }

    /**
     * Updates the q-value of the last action according to the q-Learning formula shown in
     * equation (1) on page 85 in the paper.
     *
     * @param reward The intermediate reward.
     * @param oldState The state before executing the last action.
     * @param newState The state after executing the last action.
     * @param lastAction The last executed action.
     */
    private void updateQValue(double reward, IScreenState oldState, IScreenState newState, Action lastAction) {

        // the future reward is defined as the maximal q-value in the new state
        double futureReward = 0.0d;

        if (qValues.containsKey(newState)) {
            Map<Action, Double> actionQValueMapping = qValues.get(newState);
            futureReward = Collections.max(actionQValueMapping.values());
        }

        double qValue = reward + discountFactor * futureReward;

        MATELog.log_acc("Intermediate reward: " + reward);
        MATELog.log_acc("Future reward: " + futureReward);
        MATELog.log_acc("New q-value: " + qValue);

        qValues.get(oldState).put(lastAction, qValue);
    }

    /**
     * Checks whether the given state represents a new state and in this case the q-Value map
     * is initialised.
     *
     * @param screenState The screen state to be checked.
     */
    private void checkForNewState(IScreenState screenState) {

        // init q-values for new state
        if (!qValues.containsKey(screenState)) {
            MATELog.log_acc("New state: " + screenState);
            Map<Action, Double> actionQValueMapping = new HashMap<>();
            for (Action action : screenState.getActions()) {
                actionQValueMapping.put(action, 0.0d);
            }
            this.qValues.put(screenState, actionQValueMapping);
        }
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
            return Randomness.randomElement(lastScreenState.getActions());
        } else {
            // select the action with the highest q-value with probability 1 - epsilon
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
