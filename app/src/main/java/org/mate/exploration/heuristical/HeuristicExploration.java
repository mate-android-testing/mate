package org.mate.exploration.heuristical;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome_factory.HeuristicalChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.intent.IntentChromosomeFactory;

public class HeuristicExploration {

    // TODO: use here chromosome factory interface
    private final IChromosomeFactory heuristicChromosomeFactory;
    private final boolean alwaysReset;

    public HeuristicExploration(int maxNumEvents) {
        this(Properties.STORE_COVERAGE(), true, maxNumEvents);
    }

    // TODO: we need some sort of combined factory that makes use of heuristic exploration with intent based actions
    // that is we need to assign weights to actions, but how to different between UI, system and intent based actions
    public HeuristicExploration(boolean storeCoverage, boolean alwaysReset, int maxNumEvents, float relativeIntentAmount) {
        this.alwaysReset = alwaysReset;
        heuristicChromosomeFactory = new IntentChromosomeFactory(storeCoverage, alwaysReset, maxNumEvents, relativeIntentAmount);
    }

    public HeuristicExploration(boolean storeCoverage, boolean alwaysReset, int maxNumEvents) {
        this.alwaysReset = alwaysReset;
        heuristicChromosomeFactory = new HeuristicalChromosomeFactory(storeCoverage, alwaysReset, maxNumEvents);
    }

    public void run() {
        if (!alwaysReset) {
            MATE.uiAbstractionLayer.resetApp();
        }
        for (int i = 0; true; i++) {
            MATE.log_acc("Exploration #" + (i + 1));
            heuristicChromosomeFactory.createChromosome();
            if (!alwaysReset) {
                MATE.uiAbstractionLayer.restartApp();
            }
        }
    }
}
