package org.mate.exploration.rl.qlearning.autodroid;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;

public class AutoDroidChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     *
     * @param maxEpisodeLength
     */
    public AutoDroidChromosomeFactory(int maxEpisodeLength) {
        super(false, maxEpisodeLength);
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        return super.createChromosome();
    }

    @Override
    protected Action selectAction() {
        return super.selectAction();
    }
}
