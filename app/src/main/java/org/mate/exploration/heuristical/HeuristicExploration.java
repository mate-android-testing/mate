package org.mate.exploration.heuristical;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome_factory.HeuristicalChromosomeFactory;

public class HeuristicExploration {
    private HeuristicalChromosomeFactory heuristicChromosomeFactory;

    public HeuristicExploration(int maxNumEvents) {
        this(Properties.STORE_COVERAGE, maxNumEvents);
    }

    public HeuristicExploration(boolean storeCoverage, int maxNumEvents) {
        heuristicChromosomeFactory = new HeuristicalChromosomeFactory(storeCoverage, false, maxNumEvents);
    }

    public void run() {
        MATE.uiAbstractionLayer.resetApp();
        for (int i = 0; true; i++) {
            MATE.uiAbstractionLayer.restartApp();
            MATE.log_acc("Exploration #" + (i + 1));
            heuristicChromosomeFactory.createChromosome();
        }
    }
}
