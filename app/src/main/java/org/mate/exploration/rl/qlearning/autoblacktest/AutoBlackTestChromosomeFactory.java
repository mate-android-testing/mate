package org.mate.exploration.rl.qlearning.autoblacktest;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;

public class AutoBlackTestChromosomeFactory extends AndroidRandomChromosomeFactory {

    public AutoBlackTestChromosomeFactory() {
        super(false, 50);
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
