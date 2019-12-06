package org.mate.exploration.intent;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.intent.IntentProvider;
import org.mate.model.TestCase;
import org.mate.ui.Action;
import org.mate.ui.PrimitiveAction;

public class IntentChromosomeFactory extends AndroidRandomChromosomeFactory {

    public static final String CHROMOSOME_FACTORY_ID = "intent_chromosome_factory";

    // stores the relative amount ([0,1]) of intent based actions that should be used
    private final float relativeIntentAmount;

    private final IntentProvider intentProvider = new IntentProvider();

    public IntentChromosomeFactory(int maxNumEvents, float relativeIntentAmount) {
        super(maxNumEvents);

        assert relativeIntentAmount >= 0.0 && relativeIntentAmount <= 1.0;
        this.relativeIntentAmount = relativeIntentAmount;
    }

    public IntentChromosomeFactory(boolean storeCoverage, boolean resetApp, int maxNumEvents, float relativeIntentAmount) {
        super(storeCoverage, resetApp, maxNumEvents);

        assert relativeIntentAmount >= 0.0 && relativeIntentAmount <= 1.0;
        this.relativeIntentAmount = relativeIntentAmount;
    }


    @Override
    public IChromosome<TestCase> createChromosome() {
        return null;
    }

    @Override
    protected Action selectAction() {
        return PrimitiveAction.randomAction();
    }
}
