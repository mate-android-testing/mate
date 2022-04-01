package org.mate.exploration.rl.qlearning.autoblacktest;

import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.Algorithm;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.commons.utils.Randomness;

/**
 * Provides an implementation based on the paper 'AutoBlackTest: Automatic Black-Box Testing of
 * Interactive Applications', see https://ieeexplore.ieee.org/document/6200099.
 */
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

        for (int i = 0; i < maxNumOfEpisodes; i++) {
            MATELog.log_acc("Episode #" + (i + 1));

            /*
             * We start an episode in a random state. If we couldn't reach the randomly selected state,
             * we start the episode in that state.
             */
            IScreenState randomScreenState = Randomness.randomElement(guiModel.getAppStates());
            boolean success = uiAbstractionLayer.moveToState(randomScreenState);

            MATELog.log_acc("Starting episode in randomly selected state: " + success);

            if (!uiAbstractionLayer.isAppOpened()) {
                // start episode from main activity
                uiAbstractionLayer.restartApp();
            }

            autoBlackTestChromosomeFactory.createChromosome();
        }
    }
}
