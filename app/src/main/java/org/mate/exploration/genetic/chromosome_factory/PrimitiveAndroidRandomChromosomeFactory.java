package org.mate.exploration.genetic.chromosome_factory;

import org.mate.ui.Action;
import org.mate.ui.PrimitiveAction;

public class PrimitiveAndroidRandomChromosomeFactory extends AndroidRandomChromosomeFactory {
    public static final String CHROMOSOME_FACTORY_ID = "primitive_android_random_chromosome_factory";

    public PrimitiveAndroidRandomChromosomeFactory(int maxNumEvents) {
        super(maxNumEvents);
    }

    public PrimitiveAndroidRandomChromosomeFactory(boolean storeCoverage, boolean resetApp, int maxNumEvents) {
        super(storeCoverage, resetApp, maxNumEvents);
    }

    @Override
    protected Action selectAction() {
        return PrimitiveAction.randomAction();
    }
}
