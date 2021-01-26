package org.mate.exploration.heuristical;

import org.mate.MATE;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.intent.IntentChromosomeFactory;

public class RandomExploration implements Algorithm {
    private final AndroidRandomChromosomeFactory randomChromosomeFactory;
    private final boolean alwaysReset;

    public RandomExploration(int maxNumEvents) {
        this( true, maxNumEvents);
    }

    /**
     * Initialises the random exploration strategy including the use of
     * {@link org.mate.interaction.intent.IntentBasedAction} and {@link org.mate.interaction.intent.SystemAction}
     * in contrast to solely using {@link org.mate.interaction.ui.WidgetAction} (UI actions).
     *
     * @param alwaysReset Whether to reset the app after each creation and execution of a chromosome.
     * @param maxNumEvents The maximal number of actions for a chromosome.
     * @param relativeIntentAmount The relative amount of intents that should be used.
     */
    public RandomExploration(boolean alwaysReset, int maxNumEvents, float relativeIntentAmount) {
        this.alwaysReset = alwaysReset;
        randomChromosomeFactory = new IntentChromosomeFactory(alwaysReset, maxNumEvents, relativeIntentAmount);
    }

    public RandomExploration(boolean alwaysReset, int maxNumEvents) {
        this.alwaysReset = alwaysReset;
        randomChromosomeFactory = new AndroidRandomChromosomeFactory(alwaysReset, maxNumEvents);
    }

    public void run() {
        if (!alwaysReset) {
            MATE.uiAbstractionLayer.resetApp();
        }
        for (int i = 0; true; i++) {
            MATE.log_acc("Exploration #" + (i + 1));
            randomChromosomeFactory.createChromosome();
            if (!alwaysReset) {
                MATE.uiAbstractionLayer.restartApp();
            }
        }
    }
}
