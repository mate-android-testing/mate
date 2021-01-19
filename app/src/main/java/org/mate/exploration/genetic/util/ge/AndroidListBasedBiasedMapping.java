package org.mate.exploration.genetic.util.ge;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.ui.Action;
import org.mate.ui.WidgetAction;
import org.mate.utils.ListUtils;

import java.util.List;

public class AndroidListBasedBiasedMapping extends AndroidListBasedBiasMapping<Integer> {
    public static final int BIAS_100_PERCENT = 10000;
    public static final int BIAS_50_PERCENT = BIAS_100_PERCENT / 2;

    private final int stoppingBias; //bias towards stopping a testcase in 1 / 10000

    public AndroidListBasedBiasedMapping(int maxNumEvents) {
        this(maxNumEvents, BIAS_50_PERCENT);
    }

    public AndroidListBasedBiasedMapping(int maxNumEvents, int stoppingBiasInPerTenthousand) {
        this(true, maxNumEvents, stoppingBiasInPerTenthousand);
    }

    public AndroidListBasedBiasedMapping(boolean resetApp, int maxNumEvents, int stoppingBiasPerTenthousand) {
        super(resetApp, maxNumEvents);
        this.stoppingBias = stoppingBiasPerTenthousand;
    }

    @Override
    protected boolean finishTestCase() {
        int value = ListUtils.wrappedGet(
                        activeGenotypeChromosome.getValue(),
                        activeGenotypeCurrentCodonIndex);
        activeGenotypeCurrentCodonIndex++;
        if (value < 0) {
            value = ~value;
        }

        return value % BIAS_100_PERCENT < stoppingBias;
    }

    @Override
    protected Action selectAction() {
        List<WidgetAction> executableActions = uiAbstractionLayer.getExecutableActions();
        WidgetAction selectedAction = ListUtils.wrappedGet(
                executableActions,
                ListUtils.wrappedGet(
                        activeGenotypeChromosome.getValue(), activeGenotypeCurrentCodonIndex));
        activeGenotypeCurrentCodonIndex++;
        return selectedAction;
    }
}
