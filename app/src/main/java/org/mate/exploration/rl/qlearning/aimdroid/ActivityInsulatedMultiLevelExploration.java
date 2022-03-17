package org.mate.exploration.rl.qlearning.aimdroid;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.TestCase;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * An implementation of the paper ' AimDroid: Activity-Insulated Multi-level Automated Testing for
 * Android Applications', see https://ieeexplore.ieee.org/document/8094413.
 */
public class ActivityInsulatedMultiLevelExploration implements Algorithm {

    /**
     * The used factory to produce new chromosomes (test cases).
     */
    private final IChromosomeFactory<TestCase> aimDroidChromosomeFactory;

    /**
     * Enables the interaction with the AUT.
     */
    private final UIAbstractionLayer uiAbstractionLayer = Registry.getUiAbstractionLayer();

    /**
     * Initialises the activity insulation multi level exploration strategy.
     *
     * @param alwaysReset Whether to reset the AUT before creating a new chromosome (test case).
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public ActivityInsulatedMultiLevelExploration(boolean alwaysReset, int maxNumEvents) {
        aimDroidChromosomeFactory = new AimDroidChromosomeFactory(alwaysReset, maxNumEvents);
    }

    /**
     * Explores each activity systematically, see Algorithm 1 in the paper.
     */
    @Override
    public void run() {

        final Set<String> discoveredActivities = new HashSet<>();

        // explore in episodes each activity at least once
        Deque<String> activities = new LinkedList<>();
        activities.add(Registry.getMainActivity());
        List<String> remainingActivities = Registry.getUiAbstractionLayer().getActivityNames();
        remainingActivities.remove(Registry.getMainActivity());
        activities.addAll(remainingActivities);

        while (!activities.isEmpty()) {

            String targetActivity = activities.poll();
            MATE.log_acc("Exploring in cage: " + targetActivity);

            discoveredActivities.add(targetActivity);

            // try to directly launch the the target activity
            boolean success = uiAbstractionLayer.moveToActivity(targetActivity);

            if (!success) {
                MATE.log_acc("Couldn't move AUT into activity: " + targetActivity);
                // Remove activity from seen activities so it can be explored later if found during exploration
                // Intuition: if activity is found later, there should be a way to get from root to the activity
                discoveredActivities.remove(targetActivity);
                continue;
            }

            // explore activity in cage
            IChromosome<TestCase> chromosome = aimDroidChromosomeFactory.createChromosome();
            TestCase testCase = chromosome.getValue();

            // if R = R_3_^
            if (testCase.getCrashDetected()) {
                // TODO: only if crash is new
                activities.add(targetActivity);
            }
        }

        MATE.log_acc("Finished exploration...");
        MATE.log_acc("Discovered the following activities: " + discoveredActivities);
    }
}
