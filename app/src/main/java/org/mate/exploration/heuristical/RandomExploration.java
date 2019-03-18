package org.mate.exploration.heuristical;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.HeuristicalChromosomeFactory;

public class RandomExploration {
    private AndroidRandomChromosomeFactory randomChromosomeFactory;

    public RandomExploration(int maxNumEvents) {
        this(Properties.STORE_COVERAGE, maxNumEvents);
    }

    public RandomExploration(boolean storeCoverage, int maxNumEvents) {
        randomChromosomeFactory = new AndroidRandomChromosomeFactory(storeCoverage, false, maxNumEvents);
    }

    public void run() {
        MATE.uiAbstractionLayer.resetApp();
        for (int i = 0; true; i++) {
            MATE.uiAbstractionLayer.restartApp();
            MATE.log_acc("Exploration #" + (i + 1));
            randomChromosomeFactory.createChromosome();
        }
    }
}
