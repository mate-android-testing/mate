package org.mate.exploration.genetic.util.ge;

import org.mate.ui.Action;
import org.mate.ui.WidgetAction;
import org.mate.utils.ListUtils;

import java.util.List;

/**
 * An integer sequence to android {@link org.mate.model.TestCase} mapping with a bias parameter
 * that controls the probability of ending a test case.
 */
public class AndroidListBasedBiasedMapping extends AndroidListBasedMapping<Integer> {
    public static final int BIAS_100_PERCENT = 10000;
    public static final int BIAS_50_PERCENT = BIAS_100_PERCENT / 2;

    private final int stoppingBias; //bias towards stopping a testcase in 1 / 10000

    /**
     * Create a mapping with the default stopping bias (50%)
     */
    public AndroidListBasedBiasedMapping() {
        this(BIAS_50_PERCENT);
    }

    /**
     * Create a mapping with the given per 10000 stopping bias, e.g. 1000 resulting in a 10%
     * stopping bias
     * @param stoppingBiasPerTenthousand stopping bias per 10000
     */
    public AndroidListBasedBiasedMapping(int stoppingBiasPerTenthousand) {
        this(true, stoppingBiasPerTenthousand);
    }

    /**
     * Create a mapping with the given per 10000 stopping bias, e.g. 1000 resulting in a 10%
     * stopping bias and an indicator whether the app should be reset before starting the
     * {@link org.mate.model.TestCase}
     * @param resetApp whether to reset the app before starting a test case
     * @param stoppingBiasPerTenthousand stopping bias per 10000
     */
    public AndroidListBasedBiasedMapping(boolean resetApp, int stoppingBiasPerTenthousand) {
        super(resetApp, -1);
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
