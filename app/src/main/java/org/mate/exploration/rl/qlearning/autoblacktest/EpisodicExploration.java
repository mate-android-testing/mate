package org.mate.exploration.rl.qlearning.autoblacktest;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.utils.Randomness;

public class EpisodicExploration implements Algorithm {

    /**
     * The used factory to produce new chromosomes (test cases).
     */
    private final AutoBlackTestChromosomeFactory autoBlackTestChromosomeFactory;

    /**
     * The current gui model.
     */
    private final IGUIModel guiModel;

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
     * @param epsilon The epsilon used in the epsilon-greedy learning policy.
     * @param discountFactor The discount factor gamma used in equation (1).
     */
    public EpisodicExploration(int maxNumOfEpisodes, int maxEpisodeLength, float epsilon,
                               float discountFactor) {
        this.maxNumOfEpisodes = maxNumOfEpisodes;
        this.guiModel = uiAbstractionLayer.getGuiModel();
        autoBlackTestChromosomeFactory
                = new AutoBlackTestChromosomeFactory(maxEpisodeLength, epsilon, discountFactor);
    }


    /**
     * Explores the AUT in episodes until the maximal number of episodes is reached or the specified
     * timeout is triggered. An episode is started in a random state.
     */
    @Override
    public void run() {

    }
}
