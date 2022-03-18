package org.mate.exploration.rl.qlearning.aimdroid;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.TestCase;
import org.mate.utils.manifest.element.ComponentDescription;

import java.util.Deque;
import java.util.HashSet;
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
     * The set of visited activities.
     */
    private final Set<String> visitedActivities = new HashSet<>();

    /**
     * Initialises the activity insulation multi level exploration strategy.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     * @param epsilon The epsilon used in the greedy learning police of SARSA.
     */
    public ActivityInsulatedMultiLevelExploration(int maxNumEvents, double epsilon) {
        aimDroidChromosomeFactory = new AimDroidChromosomeFactory(maxNumEvents, epsilon);
    }

    /**
     * Explores each activity systematically, see Algorithm 1 in the paper.
     */
    @Override
    public void run() {

        MATE.log_acc("Starting exploration...");

        /*
         * Unlike in the AimDroid paper, we add initially all activities that are exported according
         * to the manifest to the queue. Otherwise, the exploration may end very quickly, e.g. if
         * we press immediately 'BACK' on the main activity.
         */
        Deque<String> queue = new LinkedList<>();

        Set<String> exportedActivities = Registry.getManifest().getExportedActivities().stream()
                .map(ComponentDescription::getFullyQualifiedName)
                .filter(activity -> !activity.equals(Registry.getMainActivity()))
                .peek(activity -> MATE.log_acc("Exported activity: " + activity))
                .collect(Collectors.toSet());

        // we start with the main activity
        queue.add(Registry.getMainActivity());
        queue.addAll(exportedActivities);

        // explore in episodes each activity at least once
        while (!queue.isEmpty()) {
            String targetActivity = queue.poll();
            exploreInCage(queue, targetActivity);
        }

        MATE.log_acc("Finished exploration...");
        MATE.log_acc("Discovered the following activities: " + visitedActivities);
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

            stop = true;

            // try to directly launch the the target activity
            boolean success = uiAbstractionLayer.moveToActivity(targetActivity);

            if (!success) {
                MATE.log_acc("Couldn't move AUT into activity: " + targetActivity);
                // Remove activity from seen activities so it can be explored later if found during exploration
                // Intuition: if activity is found later, there should be a way to get from root to the activity

                // TODO: Maybe re-enqueue activity, but according to Dominik this will end in a endless
                //  loop of those activities that can't be targeted!
                break;
            }

            visitedActivities.add(targetActivity);

            /*
            * As in the AimDroid paper, we explore the target activity until either
            * (1) an activity or app transitions happens
            * (2) a crash occurs
            * (3) the maximal number of events per test case is reached
             */
            aimDroidChromosomeFactory.setTargetActivity(targetActivity);
            IChromosome<TestCase> chromosome = aimDroidChromosomeFactory.createChromosome();
            TestCase testCase = chromosome.getValue();

            // check whether there was an activity transition to a new activity - line 19
            String currentActivity = uiAbstractionLayer.getCurrentActivity();
            if (uiAbstractionLayer.isAppOpened() && !visitedActivities.contains(currentActivity)) {
                MATE.log_acc("Discovered new activity: " + currentActivity);
                queue.add(currentActivity);
                stop = false;
            }

            // check whether we discovered a new crash - line 24
            if (testCase.getCrashDetected()) {
                MATE.log_acc("Discovered a crash!");
                // TODO: only if crash is new
                stop = false;
            }
        }
    }
}
