package org.mate.exploration.genetic.chromosome_factory;

import org.mate.Registry;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.EspressoAction;
import org.mate.commons.utils.Randomness;
import org.mate.interaction.DeviceMgr;
import org.mate.model.TestCase;

/**
 * Provides a chromosome factory that generates {@link TestCase}s consisting of random
 * {@link EspressoAction}s.
 */
public class EspressoRandomChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * Initialises a new chromosome factory that is capable of generating random {@link TestCase}s.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public EspressoRandomChromosomeFactory(int maxNumEvents) {
        this(true, maxNumEvents);
    }

    /**
     * Initialises a new chromosome factory that is capable of generating random {@link TestCase}s.
     *
     * @param resetApp Whether to reset the AUT before creating a new chromosome (test case).
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public EspressoRandomChromosomeFactory(boolean resetApp, int maxNumEvents) {
        super(resetApp, maxNumEvents);
        // As per Espresso setup instructions: "To avoid flakiness, we highly recommend that you
        // turn off system animations on the virtual or physical devices used for testing."
        // URL: https://developer.android.com/training/testing/espresso/setup#set-up-environment
        Registry.getDeviceMgr().disableAnimations();
    }

    @Override
    protected Action selectAction() {
        return Randomness.randomElement(uiAbstractionLayer.getExecutableEspressoActions());
    }
}
