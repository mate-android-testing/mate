package org.mate.exploration.genetic.chromosome_factory;

import org.mate.Registry;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.PrimitiveAction;

/**
 * Provides a chromosome factory that generates {@link org.mate.model.TestCase}s consisting of
 * {@link PrimitiveAction}s.
 */
public class PrimitiveAndroidRandomChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * Initialises a new chromosome factory that is capable of generating test cases consisting
     * of primitive actions.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public PrimitiveAndroidRandomChromosomeFactory(int maxNumEvents) {
        super(maxNumEvents);
    }

    /**
     * Initialises a new chromosome factory that is capable of generating test cases consisting
     * of primitive actions.
     *
     * @param resetApp Whether to reset the AUT before generating a new chromosome.
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public PrimitiveAndroidRandomChromosomeFactory(boolean resetApp, int maxNumEvents) {
        super(resetApp, maxNumEvents);
    }

    /**
     * Selects the next action. Here a randomly generated {@link PrimitiveAction} is selected.
     *
     * @return Returns the selected action.
     */
    @Override
    protected Action selectAction() {
        return PrimitiveAction.randomAction(
                Registry.getUiAbstractionLayer().getCurrentActivity(),
                Registry.getUiAbstractionLayer().getScreenWidth(),
                Registry.getUiAbstractionLayer().getScreenHeight()
        );
    }
}
