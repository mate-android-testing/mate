package org.mate.exploration.heuristical;

import org.mate.Registry;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome_factory.HeuristicalChromosomeFactory;

/**
 * Provides a heuristic exploration strategy that comes from the Stoat paper.
 */
public class HeuristicExploration implements Algorithm {

    /**
     * The chromosome factory that produces {@link org.mate.model.TestCase}s consisting of
     * {@link UIAction}s, where the ui action selection process is
     * based on a widget-based weighting approach as used in Stoat.
     */
    private final HeuristicalChromosomeFactory heuristicChromosomeFactory;

    /**
     * Whether to reset the app before/after generating a new chromosome.
     */
    private final boolean alwaysReset;

    /**
     * Initialises the heuristic exploration with the maximal number of actions per test case.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public HeuristicExploration(int maxNumEvents) {
        this( true, maxNumEvents);
    }

    /**
     * Initialises the heuristic exploration with the maximal number of actions per test case
     * and whether to reset the AUT before/after creating a new chromosome.
     *
     * @param alwaysReset Whether to reset the app after each creation and execution of a chromosome.
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public HeuristicExploration(boolean alwaysReset, int maxNumEvents) {
        this.alwaysReset = alwaysReset;
        heuristicChromosomeFactory = new HeuristicalChromosomeFactory(alwaysReset, maxNumEvents);
    }

    /**
     * Invokes the heuristic exploration. In an infinite loop, chromosomes are generated and executed.
     */
    public void run() {

        if (!alwaysReset) {
            Registry.getUiAbstractionLayer().resetApp();
        }

        for (int i = 0; true; i++) {
            MATELog.log_acc("Exploration #" + (i + 1));
            heuristicChromosomeFactory.createChromosome();
            if (!alwaysReset) {
                Registry.getUiAbstractionLayer().restartApp();
            }
        }
    }
}
