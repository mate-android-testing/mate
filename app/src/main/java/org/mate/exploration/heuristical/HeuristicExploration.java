package org.mate.exploration.heuristical;

import org.mate.MATE;
import org.mate.exploration.genetic.HeuristicalChromosomeFactory;
import org.mate.ui.EnvironmentManager;

public class HeuristicExploration {
    private HeuristicalChromosomeFactory heuristicChromosomeFactory;

    public HeuristicExploration(int maxNumEvents) {
        heuristicChromosomeFactory = new HeuristicalChromosomeFactory(true, false, maxNumEvents);
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
