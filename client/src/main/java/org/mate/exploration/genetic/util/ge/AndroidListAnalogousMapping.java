package org.mate.exploration.genetic.util.ge;

import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.utils.ListUtils;

import java.util.List;

/**
 * An integer sequence to android {@link org.mate.model.TestCase} mapping where each codon is
 * directly mapped to an {@link Action} and the test case is ends when all codons where used to
 * determine an action.
 */
public class AndroidListAnalogousMapping extends AndroidListBasedMapping<Integer> {

    /**
     * The default list analogous mapping.
     */
    public AndroidListAnalogousMapping() {
        super(-1);
    }

    /**
     * Whether the current test case should be stopped.
     *
     * @return Returns {@code true} if the test case should be stopped, otherwise {@code false}
     *          is returned.
     */
    @Override
    protected boolean finishTestCase() {
        return activeGenotypeChromosome.getValue().size() == activeGenotypeCurrentCodonIndex;
    }

    /**
     * The action that should be executed next.
     *
     * @return Returns the selected action.
     */
    @Override
    protected Action selectAction() {
        List<UIAction> executableActions = uiAbstractionLayer.getExecutableUiActions();
        UIAction selectedAction = ListUtils.wrappedGet(
                executableActions,
                activeGenotypeChromosome.getValue().get(activeGenotypeCurrentCodonIndex));
        activeGenotypeCurrentCodonIndex++;
        return selectedAction;
    }
}
