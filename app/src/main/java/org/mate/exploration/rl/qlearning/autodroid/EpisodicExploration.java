package org.mate.exploration.rl.qlearning.autodroid;

import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.interaction.UIAbstractionLayer;

public class EpisodicExploration implements Algorithm {

    /**
     * The used factory to produce new chromosomes (test cases).
     */
    private final AutoDroidChromosomeFactory autoDroidChromosomeFactory;

    /**
     * Enables the interaction with the AUT.
     */
    private final UIAbstractionLayer uiAbstractionLayer = Registry.getUiAbstractionLayer();

    /**
     * The maximal number of episodes.
     */
    private final int maxNumOfEpisodes;

    /**
     * Initialises the episodic exploration.
     *
     * @param maxNumOfEpisodes The maximal number of episodes (test cases).
     * @param maxEpisodeLength The maximal length of an episode (actions per test case).
     * @param initialQValue The initial q-value for a new action.
     */
    public EpisodicExploration(int maxNumOfEpisodes, int maxEpisodeLength, double initialQValue) {
        this.maxNumOfEpisodes = maxNumOfEpisodes;
        autoDroidChromosomeFactory = new AutoDroidChromosomeFactory(maxEpisodeLength, initialQValue);
    }

    @Override
    public void run() {

    }
}
