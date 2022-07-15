package org.mate.exploration.genetic.util.ge;

import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.utils.ListUtils;

import java.util.List;

/**
 * An integer sequence to android {@link org.mate.model.TestCase} mapping where every codon based
 * decision is a 50/50 decision. That means in order to choose an {@link Action} the
 * list of available actions on the current screen state is divided in half multiple times until
 * only a single actions remains. Which half should be halved again is determined by a codon.
 */
public class AndroidListBasedEqualWeightedDecisionBiasedMapping extends AndroidListBasedBiasedMapping {

    /**
     * A equal weighted integer sequence to android {@link org.mate.model.TestCase} mapping with
     * a 50% test case ending bias.
     */
    public AndroidListBasedEqualWeightedDecisionBiasedMapping() {
        super();
    }

    /**
     * A equal weighted integer sequence to android {@link org.mate.model.TestCase} mapping with
     * the given stopping bias per 10000, e.g. 1000 resulting in a 10% stopping bias.
     *
     * @param stoppingBiasPerTenThousand The stopping bias per 10000.
     */
    public AndroidListBasedEqualWeightedDecisionBiasedMapping(int stoppingBiasPerTenThousand) {
        super(stoppingBiasPerTenThousand);
    }

    /**
     * A equal weighted integer sequence to android {@link org.mate.model.TestCase} mapping with
     * the given stopping bias per 10000, e.g. 1000 resulting in a 10% stopping bias and an
     * indicator whether the app should be reset before starting the {@link org.mate.model.TestCase}.
     *
     * @param resetApp Whether to reset the app before starting a test case.
     * @param stoppingBiasPerTenThousand The stopping bias per 10000.
     */
    public AndroidListBasedEqualWeightedDecisionBiasedMapping(boolean resetApp, int stoppingBiasPerTenThousand) {
        super(resetApp, stoppingBiasPerTenThousand);
    }

    /**
     * The action that should be executed next.
     *
     * @return Returns the selected action.
     */
    @Override
    protected Action selectAction() {
        List<UIAction> executableActions = uiAbstractionLayer.getExecutableUiActions();
        int lowBound = 0;
        int highBound = executableActions.size() - 1;
        while (lowBound != highBound) {
            if (nextBoolFromCurrentCodon()) {
                highBound = (lowBound + highBound) / 2;
            } else {
                lowBound = (lowBound + highBound) / 2;
            }
        }
        return executableActions.get(lowBound);
    }

    private boolean nextBoolFromCurrentCodon() {
        int codon = ListUtils.wrappedGet(activeGenotypeChromosome.getValue(), activeGenotypeCurrentCodonIndex);
        activeGenotypeCurrentCodonIndex++;
        return codon < 0;
    }
}
