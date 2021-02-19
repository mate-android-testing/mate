package org.mate.exploration.genetic.util.ge;

import org.mate.ui.Action;
import org.mate.ui.WidgetAction;
import org.mate.utils.ListUtils;

import java.util.List;

public class AndroidListAnalogousMapping extends AndroidListBasedMapping<Integer> {
    public AndroidListAnalogousMapping(int maxNumEvents) {
        super(maxNumEvents);
    }

    @Override
    protected boolean finishTestCase() {
        return activeGenotypeChromosome.getValue().size() == activeGenotypeCurrentCodonIndex;
    }

    @Override
    protected Action selectAction() {
        List<WidgetAction> executableActions = uiAbstractionLayer.getExecutableActions();
        WidgetAction selectedAction = ListUtils.wrappedGet(
                executableActions,
                activeGenotypeChromosome.getValue().get(activeGenotypeCurrentCodonIndex));
        activeGenotypeCurrentCodonIndex++;
        return selectedAction;
    }
}
