package org.mate.exploration.rl.qlearning.autoblacktest;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;

public class AutoBlackTestChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * The epsilon used in the epsilon-greedy learning policy.
     */
    private final float epsilon;

    /**
     * The static discount factor gamma used in equation (1).
     */
    private final float discountFactor;

    public AutoBlackTestChromosomeFactory(int maxEpisodeLength, float epsilon, float discountFactor) {
        super(false, maxEpisodeLength);
        this.epsilon = epsilon;
        this.discountFactor = discountFactor;
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
