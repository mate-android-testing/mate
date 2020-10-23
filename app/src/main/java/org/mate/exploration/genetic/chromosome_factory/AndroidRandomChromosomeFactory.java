package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunctionMultiObjective;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;
import org.mate.ui.Action;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.ui.EnvironmentManager;
import org.mate.utils.Coverage;
import org.mate.utils.Randomness;

public class AndroidRandomChromosomeFactory implements IChromosomeFactory<TestCase> {
    public static final String CHROMOSOME_FACTORY_ID = "android_random_chromosome_factory";

    protected UIAbstractionLayer uiAbstractionLayer;
    protected int maxNumEvents;
    protected boolean resetApp;
    private int actionsCount;

    public AndroidRandomChromosomeFactory(int maxNumEvents) {
        this(true, maxNumEvents);
    }

    public AndroidRandomChromosomeFactory( boolean resetApp, int maxNumEvents) {
        this.uiAbstractionLayer = MATE.uiAbstractionLayer;
        this.maxNumEvents = maxNumEvents;
        this.resetApp = resetApp;
        actionsCount = 0;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        if (resetApp) {
            uiAbstractionLayer.resetApp();
        }
        actionsCount = 0;

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (int i = 0; !finishTestCase(); i++) {
                actionsCount = i;
                if (!testCase.updateTestCase(selectAction(), String.valueOf(i))) {
                    return chromosome;
                }
            }
        } finally {
            testCase.finish(chromosome);
        }
        return chromosome;
    }

    protected boolean finishTestCase() {
        return actionsCount >= maxNumEvents;
    }

    protected Action selectAction() {
        return Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
    }
}
