package org.mate.exploration.rl.qlearning.autodroid;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.utils.Randomness;

/**
 * An implementation based on the paper 'Reinforcement Learning for Android GUI Testing', see
 * https://davidadamojr.com/wp-content/uploads/2018/11/Reinforcement-Learning-for-Android-GUI-Testing.pdf.
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
     * The current gui model.
     */
    private final IGUIModel guiModel;

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
        this.guiModel = uiAbstractionLayer.getGuiModel();
        autoDroidChromosomeFactory = new AutoDroidChromosomeFactory(maxEpisodeLength, initialQValue, pHomeButton);
    }

    /**
     * Explores the AUT in episodes until the maximal number of episodes is reached or the specified
     * timeout is triggered.
     */
    @Override
    public void run() {

        for (int i = 0; i < maxNumOfEpisodes; i++) {
            MATE.log_acc("Episode #" + (i + 1));

            /*
            * We start an episode in a random state. If we couldn't reach the randomly selected state,
            * we start the episode in that state.
             */
            IScreenState randomScreenState = Randomness.randomElement(guiModel.getAppStates());
            boolean success = uiAbstractionLayer.moveToState(randomScreenState);

            MATE.log_acc("Starting episode in random state: " + success);
            MATE.log_acc("Starting episode in activity: " + uiAbstractionLayer.getCurrentActivity());

            if (!uiAbstractionLayer.isAppOpened()) {
                // start episode from main activity
                uiAbstractionLayer.restartApp();
            }

            autoDroidChromosomeFactory.createChromosome();
        }
    }
}
