package org.mate.exploration.heuristical;

import org.mate.Registry;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.intent.IntentChromosomeFactory;

/**
 * Provides a random exploration strategy that produces {@link org.mate.model.TestCase}s consisting
 * of randomly selected actions.
 */
public class RandomExploration implements Algorithm {

    /**
     * The chromosome factory used to generate test case chromosomes.
     */
    private final AndroidRandomChromosomeFactory randomChromosomeFactory;

    /**
     * Whether to reset the app before/after generating a new chromosome.
     */
    private final boolean alwaysReset;

    /**
     * Initialises the random exploration with the maximal number of actions per test case. Only
     * uses {@link UIAction}s.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public RandomExploration(int maxNumEvents) {
        this( true, maxNumEvents);
    }

    /**
     * Initialises the random exploration strategy including the use of
     * {@link org.mate.commons.interaction.action.intent.IntentBasedAction} and
     * {@link org.mate.commons.interaction.action.intent.SystemAction} actions.
     *
     * @param alwaysReset Whether to reset the app after each creation and execution of a chromosome.
     * @param maxNumEvents The maximal number of actions per test case.
     * @param relativeIntentAmount The relative amount of intents that should be used.
     */
    public RandomExploration(boolean alwaysReset, int maxNumEvents, float relativeIntentAmount) {
        this.alwaysReset = alwaysReset;
        randomChromosomeFactory = new IntentChromosomeFactory(alwaysReset, maxNumEvents, relativeIntentAmount);
    }

    /**
     * Initialises the random exploration with the maximal number of actions per test case. Only
     * uses {@link UIAction}s.
     *
     * @param alwaysReset Whether to reset the app after each creation and execution of a chromosome.
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public RandomExploration(boolean alwaysReset, int maxNumEvents) {
        this.alwaysReset = alwaysReset;
        randomChromosomeFactory = new AndroidRandomChromosomeFactory(alwaysReset, maxNumEvents);
    }

    /**
     * Invokes the random exploration. In an infinite loop, chromosomes are generated and executed.
     */
    public void run() {

        if (!alwaysReset) {
            Registry.getUiAbstractionLayer().resetApp();
        }

        for (int i = 0; true; i++) {
            MATELog.log_acc("Exploration #" + (i + 1));
            randomChromosomeFactory.createChromosome();
            if (!alwaysReset) {
                Registry.getUiAbstractionLayer().restartApp();
            }
        }
    }
}
