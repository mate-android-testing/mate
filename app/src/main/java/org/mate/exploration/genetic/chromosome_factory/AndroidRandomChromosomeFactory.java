package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.ui.Action;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.utils.CoverageUtils;
import org.mate.utils.Randomness;

public class AndroidRandomChromosomeFactory implements IChromosomeFactory<TestCase> {
    public static final String CHROMOSOME_FACTORY_ID = "android_random_chromosome_factory";

    protected UIAbstractionLayer uiAbstractionLayer;
    protected int maxNumEvents;
    protected boolean resetApp;
    protected boolean triggerStoreCoverage;

    public AndroidRandomChromosomeFactory(int maxNumEvents) {
        this( true, maxNumEvents);
    }

    public AndroidRandomChromosomeFactory( boolean resetApp, int maxNumEvents) {
        this.uiAbstractionLayer = MATE.uiAbstractionLayer;
        this.maxNumEvents = maxNumEvents;
        this.resetApp = resetApp;
        triggerStoreCoverage = true;
    }

    public void setTriggerStoreCoverage(boolean triggerStoreCoverage) {
        this.triggerStoreCoverage = triggerStoreCoverage;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        if (resetApp) {
            uiAbstractionLayer.resetApp();
        }

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (int i = 0; i < maxNumEvents; i++) {
                if (!testCase.updateTestCase(selectAction(), String.valueOf(i))) {
                    return chromosome;
                }
            }
        } finally {
            if (triggerStoreCoverage) {
                CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
                CoverageUtils.logChromosomeCoverage(chromosome);
            }
            testCase.finish();
        }
        return chromosome;
    }

    protected Action selectAction() {
        return Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
    }
}
