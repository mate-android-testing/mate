package org.mate.exploration.genetic.util.ge;

import org.mate.ui.Action;
import org.mate.ui.WidgetAction;
import org.mate.utils.ListUtils;

import java.util.List;

public class AndroidListBasedEqualWeightedDecisionBiasedMapping extends AndroidListBasedBiasedMapping {
    public AndroidListBasedEqualWeightedDecisionBiasedMapping(int maxNumEvents) {
        super(maxNumEvents);
    }

    public AndroidListBasedEqualWeightedDecisionBiasedMapping(int maxNumEvents, int stoppingBiasInPerTenthousand) {
        super(maxNumEvents, stoppingBiasInPerTenthousand);
    }

    public AndroidListBasedEqualWeightedDecisionBiasedMapping(boolean resetApp, int maxNumEvents, int stoppingBias) {
        super(resetApp, maxNumEvents, stoppingBias);
    }

    @Override
    protected Action selectAction() {
        List<WidgetAction> executableActions = uiAbstractionLayer.getExecutableActions();
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
