package org.mate.exploration.heuristical;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;

public class RandomExploration {
    private final AndroidRandomChromosomeFactory randomChromosomeFactory;
    private final boolean alwaysReset;

    public RandomExploration(int maxNumEvents) {
        this(Properties.STORE_COVERAGE, false, maxNumEvents);
    }

    public RandomExploration(boolean storeCoverage, boolean alwaysReset, int maxNumEvents) {
        this.alwaysReset = alwaysReset;
        randomChromosomeFactory = new AndroidRandomChromosomeFactory(storeCoverage, alwaysReset, maxNumEvents);
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
