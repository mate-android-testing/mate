package org.mate.exploration.rl.qlearning.qbe.exploration;

import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;

/**
 * Performs the QLearning-Based Exploration (QBE) as outlined in Algorithm 3 on page 109.
 * The actions of the test case are sampled based on the associated Q-Matrix.
 */
public class QBEChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * Initialises a new chromosome factory that is capable of generating {@link TestCase}s.
     *
     * @param resetApp Whether to reset the AUT before creating a new chromosome (test case).
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public QBEChromosomeFactory(boolean resetApp, int maxNumEvents) {
        super(resetApp, maxNumEvents);
    }

    /**
     * Selects the action based on the Q-Matrix that should be executed next.
     *
     * @return Returns the selected action.
     */
    protected Action selectAction() {
        throw new UnsupportedOperationException("not implemented yet!");
    }
}
