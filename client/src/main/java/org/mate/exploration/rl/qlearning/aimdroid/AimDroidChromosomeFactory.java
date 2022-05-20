package org.mate.exploration.rl.qlearning.aimdroid;

import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.commons.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.ListUtils;
import org.mate.commons.utils.Randomness;
import org.mate.utils.StackTrace;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Simulates the 'exploreInCage' functionality as outlined in Algorithm 1. We explore a given activity
 * as long as possible, i.e. until one of the following conditions is met:
 * (1) an activity or app transitions happens
 * (2) a crash occurs
 * (3) the computed bound of actions is reached
 */
public class AimDroidChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * Maintains the q-values for each state and action.
     */
    private final Map<IScreenState, List<Double>> qValues;

    /**
     * The minL constant used in the bound method. Defines the minimal number of actions on a given
     * target activity.
     */
    private final int minL;

    /**
     * The maxL constant used in the bound method. Defines the maximal number of actions on a given
     * target activity.
     */
    private final int maxL;

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
    private final Set<StackTrace> stackTraces = new HashSet<>();

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

    /**
     * Initialises the AimDroid chromosome factory with the mandatory attributes.
     *
     * @param minL The minimum number of actions per test case.
     * @param maxL The maximum number of actions per test case.
     * @param epsilon The epsilon used in the greedy learning policy.
     * @param alpha The alpha used in the SARSA equation.
     * @param gamma The gamma used in the SARSA equation.
     */
    public AimDroidChromosomeFactory(int minL, int maxL, double epsilon, double alpha, double gamma) {
        super(false, maxL);
        this.minL = minL;
        this.maxL = maxL;
        this.epsilon = epsilon;
        this.alpha = alpha;
        this.gamma = gamma;
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
     * Retrieves the action that was executed last.
     *
     * @return Returns the last executed action.
     */
    public Action getLastAction() {
        return lastAction;
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
            // initialise q-value of all actions with default value 1
            visitedScreenStates.add(lastScreenState);
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
            // check whether we discovered a (new) crash or just left the app by a regular action
            if (hasCrashDetected) {
                StackTrace stackTrace = uiAbstractionLayer.getLastCrashStackTrace();

                if (!stackTraces.contains(stackTrace)) {
                    // an action leading to a new crash gets a high reward
                    stackTraces.add(stackTrace);
                    discoveredNewCrash = true;
                    return 1d;
                } else {
                    // producing the same crash multiple times is not interesting
                    return 0d;
                }
            } else {
                // an app transition or closing the app are not desired
                return -1d;
            }
        } else {
            // check for (new) activity transition
            final String currentActivity = uiAbstractionLayer.getCurrentActivity();

            if (currentActivity.equals(targetActivity)) {
                return 0.0d;
            } else {
                if (!visitedActivities.contains(currentActivity)) {
                    // new activity transitions get a high reward
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
     * Updates the q-value following equation (3) in the paper. We consider as future reward the
     * the highest q-value in the current state.
     *
     * @param reward The reward of the last executed action.
     * @param oldState The state before the last action was executed.
     * @param newState The new (current) state.
     */
    private void updateQValue(double reward, final IScreenState oldState, final IScreenState newState) {

        MATELog.log_acc("Reward for last action " + lastAction + ": " + reward);

        double oldQValue = qValues.get(oldState).get(lastActionIndex);

        // Q(s, a) ← Q(s, a) + α(r + γQ(s′, a′) − Q(s, a))
        double qValue = oldQValue + alpha * (reward + gamma * Collections.max(qValues.get(newState))
            - oldQValue);
        MATELog.log_acc("New q-value is: " + qValue);
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

        /*
        * TODO: Use hidden API feature to block activity transitions when MATE supports API level 30.
        * One can limit activity transitions by either modifying the relevant framework calls or
        * resort to the hidden API features as described on the following page:
        *       https://icsnju.github.io/AimDroid-ICSME-2017/unrooted-AimDroid.html
        * However, those hidden API features are changing from one API level to the other, in
        * particular the ActivityManager and ActivityController that are required to block activity
        * transitions. Since MATE already builds against API level 30, but only supports emulators
        * up to level 28, we should postpone this feature, since downgrading is no option.
         */

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {

                // execute action according to ɛ-greedy learning policy + track states
                IScreenState oldState = uiAbstractionLayer.getLastScreenState();
                boolean leftApp = !testCase.updateTestCase(selectAction(), actionsCount);
                IScreenState newState = checkForNewState();

                // compute reward of last action + update q-value
                double reward = computeReward(leftApp, testCase.hasCrashDetected());
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

                /*
                 * Activity-aliases that are triggered through the quick launch functionality don't
                 * show up in the chromosomes. This would in turn falsify the activity coverage
                 * computation.
                 */
                CoverageUtils.updateTestCaseChromosomeActivityCoverage(chromosome, targetActivity);

                CoverageUtils.logChromosomeCoverage(chromosome);
            }
            testCase.finish();
        }
        return chromosome;
    }

    /**
     * Computes the bound, i.e. the number of maximal actions, that should be applied on the given
     * target activity.
     *
     * @param targetActivity The target activity.
     * @return Returns the maximal number of actions that should be applied.
     */
    private int bound(String targetActivity) {

        // the number of different actions that can be applied on the given activity (any activity state)
        int actionCount = uiAbstractionLayer.getGuiModel().getActivityStates(targetActivity).stream()
                .flatMap(state -> state.getActions().stream())
                .collect(Collectors.toSet())
                .size();
        return Math.min(Math.max(minL, actionCount), maxL);
    }

    /**
     * Determines when the test case execution should be aborted. In the case of AimDroid, we want
     * explore an activity as long as possible, i.e. until the bound is reached or we left the
     * target activity.
     *
     * @return Returns {@code true} when the test case execution should be aborted, otherwise
     *         {@code false} is returned.
     */
    @Override
    protected boolean finishTestCase() {
        return !uiAbstractionLayer.getCurrentActivity().equals(targetActivity)
                || actionsCount >= bound(targetActivity);
    }

    /**
     * Selects the action that should be executed next. This conforms to equation (2) in the paper.
     *
     * @return Returns the action that should be executed next.
     */
    @Override
    protected Action selectAction() {

        IScreenState lastScreenState = uiAbstractionLayer.getLastScreenState();
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
