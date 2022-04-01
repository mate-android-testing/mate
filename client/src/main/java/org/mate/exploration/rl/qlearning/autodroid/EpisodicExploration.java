package org.mate.exploration.rl.qlearning.autodroid;

import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.Algorithm;
import org.mate.interaction.UIAbstractionLayer;

/**
 * An implementation based on the paper 'Reinforcement Learning for Android GUI Testing', see
 * https://davidadamojr.com/wp-content/uploads/2018/11/Reinforcement-Learning-for-Android-GUI-Testing.pdf.
 * Since the authors didn't provide any particular name for their approach, we stick to the name
 * of the tool in which the approach was implemented.
 */
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
     * @param pHomeButton The probability for selecting the home button as next action.
     */
    public EpisodicExploration(int maxNumOfEpisodes, int maxEpisodeLength, float initialQValue,
                               float pHomeButton) {
        this.maxNumOfEpisodes = maxNumOfEpisodes;
        autoDroidChromosomeFactory = new AutoDroidChromosomeFactory(maxEpisodeLength, initialQValue, pHomeButton);
    }

    /**
     * Explores the AUT in episodes until the maximal number of episodes is reached or the specified
     * timeout is triggered. This is similar to the approach taken in AutoBlackTest and replaces
     * the mentioned test suite completion criterion in the original paper. Unlike AutoBlackTest,
     * each episode is started from the main activity.
     */
    @Override
    public void run() {
        for (int i = 0; i < maxNumOfEpisodes; i++) {
            MATELog.log_acc("Episode #" + (i + 1));
            autoDroidChromosomeFactory.createChromosome();
            uiAbstractionLayer.restartApp();
        }
    }
}
