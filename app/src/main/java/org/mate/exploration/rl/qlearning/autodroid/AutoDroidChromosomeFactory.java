package org.mate.exploration.rl.qlearning.autodroid;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;

public class AutoDroidChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * The initial q-value for a new action.
     */
    private final float initialQValue;

    /**
     * The probability for selecting the home button as next action.
     */
    private final float pHomeButton;

    public AutoDroidChromosomeFactory(int maxEpisodeLength, float initialQValue, float pHomeButton) {
        super(false, maxEpisodeLength);
        this.initialQValue = initialQValue;
        this.pHomeButton = pHomeButton;
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
