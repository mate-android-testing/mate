package org.mate.exploration.genetic.util.ge;

import org.mate.ui.Action;
import org.mate.ui.WidgetAction;
import org.mate.utils.ListUtils;

import java.util.List;

public class AndroidListBasedBinaryEqualWeightedDecisionMapping extends AndroidListBasedMapping<Boolean> {
    public AndroidListBasedBinaryEqualWeightedDecisionMapping(int maxNumEvents) {
        this(true, maxNumEvents);
    }

    public AndroidListBasedBinaryEqualWeightedDecisionMapping(boolean resetApp, int maxNumEvents) {
        super(resetApp, maxNumEvents);
    }

    @Override
    protected boolean finishTestCase() {
        return nextBoolFromCurrentCodon();
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
        activeGenotypeCurrentCodonIndex++;
        return ListUtils.wrappedGet(activeGenotypeChromosome.getValue(), activeGenotypeCurrentCodonIndex - 1);
    }
}
