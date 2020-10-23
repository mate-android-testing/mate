package org.mate.exploration.genetic.util.ge;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.model.TestCase;
import org.mate.ui.Action;
import org.mate.ui.WidgetAction;
import org.mate.utils.ListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroidListBasedBiasedMapping extends AndroidRandomChromosomeFactory implements IGenotypePhenotypeMapping<List<Integer>, TestCase> {
    public static final int BIAS_100_PERCENT = 10000;
    public static final int BIAS_50_PERCENT = BIAS_100_PERCENT / 2;

    private final Map<IChromosome<List<Integer>>, IChromosome<TestCase>> associatedPhenotypeChromosome;
    private IChromosome<List<Integer>> activeGenotypeChromosome;
    private int activeGenotypeCurrentCodonIndex;
    private final int stoppingBias; //bias towards stopping a testcase in 1 / 10000

    public AndroidListBasedBiasedMapping(int maxNumEvents) {
        this(maxNumEvents, BIAS_50_PERCENT);
    }

    public AndroidListBasedBiasedMapping(int maxNumEvents, int stoppingBiasInPerTenthousand) {
        this(true, maxNumEvents, stoppingBiasInPerTenthousand);
    }

    public AndroidListBasedBiasedMapping(boolean resetApp, int maxNumEvents, int stoppingBias) {
        super(resetApp, maxNumEvents);
        this.stoppingBias = stoppingBias;
        associatedPhenotypeChromosome = new HashMap<>();
        activeGenotypeChromosome = null;
        activeGenotypeCurrentCodonIndex = 0;
    }

    @Override
    public IChromosome<TestCase> map(IChromosome<List<Integer>> genotype) {
        IChromosome<TestCase> phenotypeChromosome = associatedPhenotypeChromosome.get(genotype);
        if (phenotypeChromosome == null) {
            activeGenotypeChromosome = genotype;
            activeGenotypeCurrentCodonIndex = 0;
            return createChromosome();
        }
        return phenotypeChromosome;
    }

    @Override
    public IChromosome<TestCase> associatedPhenotype(IChromosome<List<Integer>> genotype) {
        return associatedPhenotypeChromosome.get(genotype);
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        IChromosome<TestCase> phenotypeChromosome = super.createChromosome();
        associatedPhenotypeChromosome.put(activeGenotypeChromosome, phenotypeChromosome);
        activeGenotypeChromosome = null;
        activeGenotypeCurrentCodonIndex = 0;
        return phenotypeChromosome;
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
