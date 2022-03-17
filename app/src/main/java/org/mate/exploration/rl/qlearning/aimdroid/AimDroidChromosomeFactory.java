package org.mate.exploration.rl.qlearning.aimdroid;

import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;

/**
 * Simulates the 'exploreInCage' functionality as outlined in Algorithm 1. We explore a given activity
 * as long as possible, i.e. until one of the following conditions is met:
 * (1) The maximal number of events is reached.
 * (2) A crash occurred.
 * (3) An activity transition happened.
 */
public class AimDroidChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * The activity that should be explored intensively.
     */
    private final String targetActivity;

    public AimDroidChromosomeFactory(boolean resetApp, int maxNumEvents) {
        super(resetApp, maxNumEvents);
        targetActivity = uiAbstractionLayer.getCurrentActivity();
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
}
