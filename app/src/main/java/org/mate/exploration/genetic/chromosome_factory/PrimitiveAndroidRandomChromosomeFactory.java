package org.mate.exploration.genetic.chromosome_factory;

import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.PrimitiveAction;

public class PrimitiveAndroidRandomChromosomeFactory extends AndroidRandomChromosomeFactory {

    public PrimitiveAndroidRandomChromosomeFactory(int maxNumEvents) {
        super(maxNumEvents);
    }

    public PrimitiveAndroidRandomChromosomeFactory(boolean resetApp, int maxNumEvents) {
        super(resetApp, maxNumEvents);
    }

    @Override
    protected Action selectAction() {
        return PrimitiveAction.randomAction();
    }
}
