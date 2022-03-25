package org.mate.exploration.rl.qlearning.autoblacktest;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.coverage.CoverageUtils;

import java.util.HashMap;
import java.util.Map;

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

                IScreenState currentState = uiAbstractionLayer.getLastScreenState();

                Action nextAction = selectAction();
                MATE.log_acc("Next action: " + nextAction);
                boolean leftApp = !testCase.updateTestCase(nextAction, actionsCount);

                if (leftApp) {
                    MATE.log_acc("We left the app!");

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
                IScreenState newState = uiAbstractionLayer.getLastScreenState();
                double reward = computeReward(currentState, newState, nextAction);
                updateQValue(reward, currentState, newState);
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

    private void updateQValue(double reward, IScreenState oldState, IScreenState newState) {

    }

    @Override
    protected Action selectAction() {
        return super.selectAction();
    }
}
