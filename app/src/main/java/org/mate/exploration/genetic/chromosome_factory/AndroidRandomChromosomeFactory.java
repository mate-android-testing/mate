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
    private int maxNumEvents;
    private boolean storeCoverage;
    private boolean resetApp;

    public AndroidRandomChromosomeFactory(int maxNumEvents) {
        this(Properties.STORE_COVERAGE(), true, maxNumEvents);
    }

    public AndroidRandomChromosomeFactory(boolean storeCoverage, boolean resetApp, int maxNumEvents) {
        this.uiAbstractionLayer = MATE.uiAbstractionLayer;
        this.maxNumEvents = maxNumEvents;
        this.storeCoverage = storeCoverage;
        this.resetApp = resetApp;
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

            testCase.print();

            if (Properties.COVERAGE() == Coverage.BRANCH_COVERAGE) {
                BranchDistanceFitnessFunctionMultiObjective.retrieveFitnessValues(chromosome);
            }

            //store coverage in any case
            if (storeCoverage) {

                if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
                    //TODO: remove hack, when better solution implemented (query fitness function)
                    Registry.getEnvironmentManager().storeCoverageData(chromosome, null);
                    LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);

                    MATE.log_acc("Coverage of: " + chromosome.toString() + ": " + Registry.getEnvironmentManager()
                            .getCoverage(chromosome));

                } else if (Properties.COVERAGE() == Coverage.BRANCH_COVERAGE) {

                    // TODO: this should be depended on which fitness function is used
                    // BranchDistanceFitnessFunction.retrieveFitnessValues(chromosome);

                    Registry.getEnvironmentManager().storeBranchCoverage(chromosome);

                    MATE.log_acc("Coverage of: " + chromosome.toString() + ": " + Registry.getEnvironmentManager()
                            .getBranchCoverage(chromosome));
                }

                MATE.log_acc("Found crash: " + String.valueOf(chromosome.getValue().getCrashDetected()));
            }
        }
        return chromosome;
    }

    protected Action selectAction() {
        return Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
    }
}
