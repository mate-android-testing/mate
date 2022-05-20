package org.mate.exploration.rl.qlearning.aimdroid;

import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.ActionType;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.utils.manifest.element.ComponentDescription;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation of the paper 'AimDroid: Activity-Insulated Multi-level Automated Testing for
 * Android Applications', see https://ieeexplore.ieee.org/document/8094413.
 */
public class ActivityInsulatedMultiLevelExploration implements Algorithm {

    /**
     * The chromosome factory used to produce test case chromosomes targeting a specific activity.
     */
    private final AimDroidChromosomeFactory aimDroidChromosomeFactory;

    /**
     * The chromosome factory used to generate random test case chromosomes.
     */
    private final AndroidRandomChromosomeFactory randomChromosomeFactory;

    /**
     * Enables the interaction with the AUT.
     */
    private final UIAbstractionLayer uiAbstractionLayer = Registry.getUiAbstractionLayer();

    /**
     * The number of allowed activity launch failures. If an activity reaches this limit, the
     * activity won't be re-enqueued in the working queue.
     */
    private static final int MAX_ACTIVITY_LAUNCH_FAILURES = 3;

    /**
     * Tracks how often an activity couldn't be launched by quick launch or a path in the gui model.
     */
    private final Map<String, Integer> activityLaunchFailures;

    /**
     * Initialises the activity insulation multi level exploration strategy.
     *
     * @param minL The minimal number of actions per test case.
     * @param maxL The maximal number of actions per test case.
     * @param epsilon The epsilon used in the greedy learning policy.
     * @param alpha The alpha used in the SARSA equation.
     * @param gamma The gamma used in the SARSA equation.
     */
    public ActivityInsulatedMultiLevelExploration(int minL, int maxL, double epsilon, double alpha,
                                                  double gamma) {
        aimDroidChromosomeFactory = new AimDroidChromosomeFactory(minL, maxL, epsilon, alpha, gamma);
        randomChromosomeFactory = new AndroidRandomChromosomeFactory(true, maxL);
        activityLaunchFailures = new HashMap<>();
    }

    /**
     * Explores each activity systematically, see Algorithm 1 in the paper.
     */
    @Override
    public void run() {

        /*
         * Unlike in the AimDroid paper, we add initially all activities that are exported according
         * to the manifest to the queue. Otherwise, the exploration may end very quickly, e.g. if
         * we press immediately 'BACK' on the main activity.
         */
        Deque<String> queue = new LinkedList<>();

        Set<String> exportedActivities = Registry.getManifest().getExportedActivities().stream()
                .map(ComponentDescription::getFullyQualifiedName)
                .filter(activity -> !activity.equals(Registry.getMainActivity()))
                .collect(Collectors.toSet());

        // we start with the main activity
        queue.add(Registry.getMainActivity());
        queue.addAll(exportedActivities);

        // explore in episodes each activity at least once
        while (!queue.isEmpty()) {
            String targetActivity = queue.poll();
            exploreInCage(queue, targetActivity);
        }

        /*
        * Although this is not explicitly stated in the AimDroid paper, the algorithm proceeds with
        * a random exploration starting from the main activity until the search budget is exhausted.
         */
        MATELog.log_acc("Running random exploration until time out is reached...");
        while (true) {
            MATELog.log_acc("Sampling new random chromosome...");
            randomChromosomeFactory.createChromosome();
        }
    }

    /**
     * Explores the given target activity in a cage, see line 9 of Algorithm 1.
     *
     * @param queue The working queue containing the activities that get explored one after each other.
     * @param targetActivity The target activity.
     */
    private void exploreInCage(Deque<String> queue, String targetActivity) {

        boolean stop = false;

        while (!stop) { // line 10 in Algorithm 1

            MATELog.log_acc("Exploring in cage: " + targetActivity);

            if (!uiAbstractionLayer.isAppOpened()) {
                // TODO: Evaluate whether restart helps in reaching a target activity.
                uiAbstractionLayer.restartApp();
            }

            stop = true;

            // try to directly launch the target activity
            boolean success = uiAbstractionLayer.moveToActivity(targetActivity);

            if (!success) {
                MATELog.log_acc("Couldn't move AUT into activity: " + targetActivity);

                /*
                * Unlike in the AimDroid paper, we re-enqueue to some degree an activity that
                * couldn't be launched. There is a chance that a path to the target activity gets
                * discovered later in the exploration process, but at some point in time we may end
                * up re-enqueuing the same activity again and again. Thus, there is an upper limit
                * for the number of retries per activity.
                 */
                int faultyLaunches = activityLaunchFailures.getOrDefault(targetActivity, 0) + 1;
                activityLaunchFailures.put(targetActivity, faultyLaunches);

                if (faultyLaunches < MAX_ACTIVITY_LAUNCH_FAILURES) {
                    queue.add(targetActivity);
                }
                break;
            }

            /*
            * As in the AimDroid paper, we explore the target activity until either
            * (1) an activity or app transitions happens
            * (2) a crash occurs
            * (3) the computed bound of actions is reached
             */
            aimDroidChromosomeFactory.setTargetActivity(targetActivity);
            aimDroidChromosomeFactory.createChromosome();

            // check whether there was an activity transition to a new activity - line 19
            String currentActivity = uiAbstractionLayer.getCurrentActivity();
            if (aimDroidChromosomeFactory.hasDiscoveredNewActivity()) {
                MATELog.log_acc("Discovered a new activity: " + currentActivity);
                queue.add(currentActivity);
                stop = false;

                /*
                * Since we can't block activity transitions right now, we should re-enqueue the
                * target activity again unlike in the AimDroid paper. Otherwise, we may execute
                * only a single action on the target activity in the worst case if the selected
                * action leads to an activity transition, since a new activity transition gets the
                * highest reward initially, which in turn leads to re-selecting the same action.
                * Now, the action gets a reward of 0, but since we couldn't block activity
                * transitions, we end the exploration in cage and throw away the activity from the
                * working queue.
                 */
                queue.add(targetActivity);
            }

            // check whether we discovered a new crash - line 24
            if (aimDroidChromosomeFactory.hasDiscoveredNewCrash()) {
                MATELog.log_acc("Discovered a new crash!");
                stop = false;
            }

            /*
            * If we immediately would select the BACK action on the main activity and leave the app,
            * the exploration of the main activity ends early. Furthermore, if this would happen in
            * the original AimDroid approach, the entire exploration ends because the working queue
            * is initially on filled with the main activity. Since the reward is negative for an
            * app transition (R2), it is unlikely that the BACK action is selected again.
             */
            Action lastAction = aimDroidChromosomeFactory.getLastAction();

            if (lastAction instanceof UIAction
                    && ((UIAction) lastAction).getActionType() == ActionType.BACK
                    && Registry.getMainActivity().equals(targetActivity)
                    && !uiAbstractionLayer.isAppOpened()) {
                MATELog.log_acc("Leaving main activity through clicking BACK button!");
                stop = false;
            }
        }
    }
}
