package org.mate.exploration.rl.qlearning.aimdroid;

import org.mate.MATE;
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
     * The epsilon used in the ɛ-greedy learning policy, see equation (2).
     */
    private final double epsilon;

    /**
     * The alpha used in the SARSA equation (3).
     */
    private final double alpha;

    /**
     * The gamma used in the SARSA equation (3).
     */
    private final double gamma;

    /**
     * We need to memorize the last action to compute its reward and update the q-Value.
     */
    private Action lastAction;

    /**
     * We need to memorize the index of the last action to compute its reward and update the q-Value.
     */
    private int lastActionIndex;

    /**
     * The activity that should be explored intensively.
     */
    private String targetActivity;

    /**
     * The set of visited activities.
     */
    private final Set<String> visitedActivities = new HashSet<>();

    /**
     * The set of recorded stack traces (crashes).
     */
    private final Set<String> stackTraces = new HashSet<>();

    /**
     * The set of visited screen states.
     */
    private final Set<IScreenState> visitedScreenStates = new HashSet<>();

    /**
     * Whether the last test case discovered a new crash.
     */
    private boolean discoveredNewCrash = false;

    /**
     * Whether the last test case discovered a new activity.
     */
    private boolean discoveredNewActivity = false;

    public AimDroidChromosomeFactory(int maxNumEvents, double epsilon, double alpha, double gamma) {
        super(false, maxNumEvents);
        this.epsilon = epsilon;
        this.alpha = alpha;
        this.gamma = gamma;
        guiModel = uiAbstractionLayer.getGuiModel();
        qValues = new HashMap<>();
    }

    /**
     * Sets the target activity to the given activity.
     *
     * @param targetActivity The new target activity.
     */
    public void setTargetActivity(String targetActivity) {
        this.targetActivity = targetActivity;
    }

    /**
     * Whether the last test case caused a new crash.
     *
     * @return Returns {@code true} if the last test case caused a new crash, otherwise {@code false}
     *         is returned.
     */
    public boolean hasDiscoveredNewCrash() {
        return discoveredNewCrash;
    }

    /**
     * Whether the last test case discovered a new activity.
     *
     * @return Returns {@code true} if the last test case discovered a new activity, otherwise
     *         {@code false} is returned.
     */
    public boolean hasDiscoveredNewActivity() {
        return discoveredNewActivity;
    }

    /**
     * Retrieves the set of visited activities.
     *
     * @return Returns the visited activities.
     */
    public Set<String> getVisitedActivities() {
        return visitedActivities;
    }

    /**
     * Checks whether we reached a new state. In this case, we need to initialise the q-Values
     * to the default value of 1.
     *
     * @return Returns the current state.
     */
    private IScreenState checkForNewState() {

        IScreenState lastScreenState = uiAbstractionLayer.getLastScreenState();

        if (!visitedScreenStates.contains(lastScreenState)) {
            MATE.log_acc("Discovered new state: " + lastScreenState);
            // initialise qValue of all actions with default value 1
            visitedScreenStates.add(lastScreenState);
            MATE.log_acc("Initialising q-Values...");
            int numberOfActions = lastScreenState.getActions().size();
            List<Double> initialQValues
                    = new ArrayList<>(Collections.nCopies(numberOfActions, 1.0));
            qValues.put(lastScreenState, initialQValues);
        }
        return lastScreenState;
    }

    /**
     * Computes the reward for the last executed action, see equation (1) in the paper.
     *
     * @param leftApp Whether the last action lead to leaving the app.
     * @param hasCrashDetected Whether a crash has been caused by the last action.
     * @return Returns the computed reward for the last action.
     */
    private double computeReward(boolean leftApp, boolean hasCrashDetected) {

        if (leftApp) {

            // check whether we discovered a new crash
            if (hasCrashDetected) {

                String stackTrace = uiAbstractionLayer.getLastCrashStackTrace();

                if (!stackTraces.contains(stackTrace)) {
                    MATE.log_acc("Detected a new crash!");
                    stackTraces.add(stackTrace);
                    discoveredNewCrash = true;
                    return 1d;
                } else {
                    return 0d;
                }

            } else {

                // this must be a regular app transition
                return -1d;
            }
        } else {

            final String currentActivity = uiAbstractionLayer.getCurrentActivity();

            // check for activity transition
            if (currentActivity.equals(targetActivity)) {
                MATE.log_acc("Still on target activity!");
                return 0.0d;
            } else {
                MATE.log_acc("Activity transition!");
                if (!visitedActivities.contains(currentActivity)) {
                    MATE.log_acc("New activity transition: " + currentActivity);
                    visitedActivities.add(currentActivity);
                    discoveredNewActivity = true;
                    return 1d;
                } else {
                    return 0.0d;
                }
            }
        }
    }

    /**
     * Updates the q-Value following equation (3) in the paper. We consider as future reward the
     * the highest q-Value in the current state.
     *
     * @param reward The reward of the last executed action.
     * @param oldState The state before the last action was executed.
     * @param newState The new (current) state.
     */
    private void updateQValue(double reward, final IScreenState oldState, final IScreenState newState) {

        double oldQValue = qValues.get(oldState).get(lastActionIndex);

        // Q(s, a) ← Q(s, a) + α(r + γQ(s′, a′) − Q(s, a))
        double qValue = oldQValue + alpha * (reward + gamma * Collections.max(qValues.get(newState))
            - oldQValue);
        MATE.log_acc("New qValue is: " + qValue);
        qValues.get(oldState).set(lastActionIndex, qValue);
    }

    /**
     * Generates a new chromosome (test case) that is filled with actions until a transition (app
     * or activity) happens, a crash occurs or the maximal number of events is reached.
     *
     * @return Returns the produced chromosome.
     */
    @Override
    public IChromosome<TestCase> createChromosome() {

        visitedActivities.add(targetActivity);
        discoveredNewCrash = false;
        discoveredNewActivity = false;

        // initialise q-values in case of new state
        checkForNewState();

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {

                // execute action according to ɛ-greedy learning policy + track states
                IScreenState oldState = uiAbstractionLayer.getLastScreenState();
                boolean leftApp = !testCase.updateTestCase(selectAction(), actionsCount);
                IScreenState newState = checkForNewState();

                // compute reward of last action + update q-Value
                double reward = computeReward(leftApp, testCase.hasCrashDetected());
                MATE.log_acc("Reward for last action " + lastAction + ": " + reward);
                updateQValue(reward, oldState, newState);

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
     *         {@code false} is returned.
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

        IScreenState lastScreenState = uiAbstractionLayer.getLastScreenState();
        MATE.log_acc("Selecting action on state: " + lastScreenState);
        double rnd = Randomness.getRnd().nextDouble();

        if (rnd < epsilon) {
            // select randomly
            lastActionIndex = Randomness.randomIndex(lastScreenState.getActions());
            lastAction = lastScreenState.getActions().get(lastActionIndex);
        } else {
            // pick the action with the highest q-value, choose random if there are multiple
            List<Double> qValues = this.qValues.get(lastScreenState);
            List<Integer> bestQValueIndices = ListUtils.getMaximaPositions(qValues);
            lastActionIndex = Randomness.randomElement(bestQValueIndices);
            lastAction = lastScreenState.getActions().get(lastActionIndex);
        }
        return lastAction;
    }
}
