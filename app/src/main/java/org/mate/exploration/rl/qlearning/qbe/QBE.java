package org.mate.exploration.rl.qlearning.qbe;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.exploration.rl.qlearning.qbe.chromosome_factory.QBEChromosomeFactory;
import org.mate.exploration.rl.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.model.fsm.qbe.ELTSSerializer;
import org.mate.model.fsm.qbe.QBEModel;

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
        try {
            while (true) {
                randomChromosomeFactory.createChromosome();
            }
        } finally {
            serializeELTS();
        }
    }

    private void serializeELTS() {
        if (!Properties.QBE_RECORD_TRANSITION_SYSTEM())
            return;

        QBEModel model = (QBEModel) Registry.getUiAbstractionLayer().getGuiModel();
        new ELTSSerializer().serialize(model.getELTS());
        Registry.getEnvironmentManager().fetchTransitionSystem();
    }
}
