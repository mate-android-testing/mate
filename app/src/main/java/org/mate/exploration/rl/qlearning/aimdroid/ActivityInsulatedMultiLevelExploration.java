package org.mate.exploration.rl.qlearning.aimdroid;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.utils.manifest.element.ComponentDescription;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation of the paper ' AimDroid: Activity-Insulated Multi-level Automated Testing for
 * Android Applications', see https://ieeexplore.ieee.org/document/8094413.
 */
public class ActivityInsulatedMultiLevelExploration implements Algorithm {

    /**
     * The used factory to produce new chromosomes (test cases).
     */
    private final AimDroidChromosomeFactory aimDroidChromosomeFactory;

    /**
     * Enables the interaction with the AUT.
     */
    private final UIAbstractionLayer uiAbstractionLayer = Registry.getUiAbstractionLayer();

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

        MATE.log_acc("Discovered the following activities: "
                + aimDroidChromosomeFactory.getVisitedActivities());
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

            MATE.log_acc("Exploring in cage: " + targetActivity);

            if (!uiAbstractionLayer.isAppOpened()) {
                // TODO: Evaluate whether restart helps in reaching a target activity.
                uiAbstractionLayer.restartApp();
            }

            stop = true;

            // try to directly launch the the target activity
            boolean success = uiAbstractionLayer.moveToActivity(targetActivity);

            if (!success) {
                MATE.log_acc("Couldn't move AUT into activity: " + targetActivity);
                // TODO: Evaluate whether re-enqueuing is helpful.
                queue.add(targetActivity);
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
                MATE.log_acc("Discovered a new activity: " + currentActivity);
                queue.add(currentActivity);
                stop = false;

                /*
                * TODO: Verify that the q-value is lower after re-selection than at least other actions
                *  that haven't been executed on the same state, otherwise the problematic as
                *  described below appears again and again.
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
                MATE.log_acc("Discovered a new crash!");
                stop = false;
            }
        }
    }
}
