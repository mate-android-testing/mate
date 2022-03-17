package org.mate.exploration.rl.qlearning.aimdroid;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.action.Action;
import org.mate.model.IGUIModel;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.ListUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simulates the 'exploreInCage' functionality as outlined in Algorithm 1. We explore a given activity
 * as long as possible, i.e. until one of the following conditions is met:
 * (1) The maximal number of events is reached.
 * (2) A crash occurred.
 * (3) An activity transition happened.
 */
public class AimDroidChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * The current gui model.
     */
    private final IGUIModel guiModel;

    /**
     * Maintains the q-values for each state and action.
     */
    private final Map<IScreenState, List<Double>> qValues;

    /**
     * The epsilon used in the ɛ-greedy learning policy of SARSA.
     */
    private final double epsilon;

    /**
     * The activity that should be explored intensively.
     */
    private String targetActivity;

    /**
     * The set of visited screen states.
     */
    private final Set<IScreenState> visitedScreenStates = new HashSet<>();

    public AimDroidChromosomeFactory(boolean resetApp, int maxNumEvents, double epsilon) {
        super(resetApp, maxNumEvents);
        this.epsilon = epsilon;
        guiModel = uiAbstractionLayer.getGuiModel();
        qValues = new HashMap<>();
    }

    public String getTargetActivity() {
        return targetActivity;
    }

    public void setTargetActivity(String targetActivity) {
        this.targetActivity = targetActivity;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {

        // TODO: set target activity here

        IScreenState lastScreenState = uiAbstractionLayer.getLastScreenState();

        if (!visitedScreenStates.contains(lastScreenState)) {
            // initialise qValue of all actions with default value 1
            visitedScreenStates.add(lastScreenState);
            int numberOfActions = lastScreenState.getActions().size();
            List<Double> initialQValues
                    = new ArrayList<>(Collections.nCopies(numberOfActions, 1.0));
            qValues.put(lastScreenState, initialQValues);
        }

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {

                boolean leftApp = !testCase.updateTestCase(selectAction(), actionsCount);

                // TODO: compute reward of last action + update q-values

                if (leftApp) {
                    return chromosome;
                }
            }
        } finally {
            if (!isTestSuiteExecution) {
                /*
                 * If we deal with a test suite execution, the storing of coverage
                 * and fitness data is handled by the AndroidSuiteRandomChromosomeFactory itself.
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
     * Determines when the test case execution should be aborted. In the case of AimDroid, we want
     * explore an activity as long as possible, i.e. until the maximal number of events is reached
     * or we left the activity.
     *
     * @return Returns {@code true} when the test case execution should be aborted, otherwise
     *          {@code false} is returned.
     */
    @Override
    protected boolean finishTestCase() {
        return super.finishTestCase() || !uiAbstractionLayer.getCurrentActivity().equals(targetActivity);
    }

    /**
     * Selects the action that should be executed next. This conforms to equation (2) in the paper.
     *
     * @return Returns the action that should be executed next.
     */
    @Override
    protected Action selectAction() {
        double rnd = Randomness.getRnd().nextDouble();
        if (rnd < epsilon) {
            IScreenState lastScreenState = uiAbstractionLayer.getLastScreenState();

            // pick the action with the highest q-value, choose random if there are multiple
            List<Double> qValues = this.qValues.get(lastScreenState);
            List<Integer> bestQValueIndices = ListUtils.getMaximaPositions(qValues);
            int selectedActionIndex = Randomness.randomElement(bestQValueIndices);
            return lastScreenState.getActions().get(selectedActionIndex);
        } else {
            // select randomly
            return super.selectAction();
        }
    }
}
