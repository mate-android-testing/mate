package org.mate.exploration.rl.qlearning.qbe;

import org.mate.exploration.Algorithm;
import org.mate.exploration.rl.qlearning.qbe.chromosome_factory.QBEChromosomeFactory;
import org.mate.exploration.rl.qlearning.qbe.exploration.ExplorationStrategy;

/**
 * Provied the QBE Q-learning based algorithm.
 */
public final class QBE implements Algorithm {

    /**
     * The chromosome factory used to generate test case chromosomes.
     */
    private final QBEChromosomeFactory randomChromosomeFactory;

    /**
     * Initialises the random exploration with the maximal number of actions per test case. Only
     * uses {@link org.mate.interaction.action.ui.UIAction}s.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public QBE(int maxNumEvents, ExplorationStrategy explorationStrategy) {
        randomChromosomeFactory = new QBEChromosomeFactory(maxNumEvents, explorationStrategy);
    }

    /**
     * Invokes the random exploration. In an infinite loop, chromosomes are generated and executed.
     */
    public void run() {
        while (true) {
            randomChromosomeFactory.createChromosome();
        }

        // TODO: Serialize ELTS.
    }
}
