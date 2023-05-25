package org.mate.exploration.heuristical;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.MotifAlphaChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.MotifChromosomeFactory;

/**
 * Provides a random exploration strategy that produces {@link org.mate.model.TestCase}s consisting
 * of randomly selected actions that include motif actions with a certain probability.
 */
public class RandomMotifExploration implements Algorithm {

    /**
     * The chromosome factory used to generate test case chromosomes.
     */
    private final AndroidRandomChromosomeFactory motifChromosomeFactory;

    /**
     * Whether to reset the app before/after generating a new chromosome.
     */
    private final boolean alwaysReset;

    /**
     * Initialises the random exploration strategy.
     *
     * @param alwaysReset Whether to reset the app after each creation and execution of a chromosome.
     * @param maxNumEvents The maximal number of actions per test case.
     * @param relativeMotifActionAmount The relative amount of motif actions that should be used.
     */
    public RandomMotifExploration(boolean alwaysReset, int maxNumEvents, float relativeMotifActionAmount) {
        this.alwaysReset = alwaysReset;
        motifChromosomeFactory = new MotifChromosomeFactory(maxNumEvents, relativeMotifActionAmount);
    }

    /**
     * Initialises the random exploration with the specified alpha that controls the likelihood of
     * motif actions.
     *
     * @param alwaysReset Whether to reset the app after each creation and execution of a chromosome.
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public RandomMotifExploration(boolean alwaysReset, int maxNumEvents) {
        this.alwaysReset = alwaysReset;
        motifChromosomeFactory = new MotifAlphaChromosomeFactory(maxNumEvents, Properties.MOTIF_ALPHA());
    }

    /**
     * Invokes the random exploration. In an infinite loop, chromosomes are generated and executed.
     */
    @Override
    public void run() {

        if (!alwaysReset) {
            Registry.getUiAbstractionLayer().resetApp();
        }

        for (int i = 0; true; i++) {
            MATE.log_acc("Exploration #" + (i + 1));
            motifChromosomeFactory.createChromosome();
            if (!alwaysReset) {
                Registry.getUiAbstractionLayer().restartApp();
            }
        }
    }
}
