package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.ui.Action;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.utils.CoverageUtils;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;

public class AndroidRandomChromosomeFactory implements IChromosomeFactory<TestCase> {
    public static final String CHROMOSOME_FACTORY_ID = "android_random_chromosome_factory";

    protected UIAbstractionLayer uiAbstractionLayer;
    protected int maxNumEvents;
    protected boolean resetApp;
    protected boolean isTestSuiteExecution;
    private int actionsCount;

    public AndroidRandomChromosomeFactory(int maxNumEvents) {
        this(true, maxNumEvents);
    }

    public AndroidRandomChromosomeFactory( boolean resetApp, int maxNumEvents) {
        this.uiAbstractionLayer = MATE.uiAbstractionLayer;
        this.maxNumEvents = maxNumEvents;
        this.resetApp = resetApp;
        isTestSuiteExecution = false;
        actionsCount = 0;
    }

    // TODO: might be replaceable with chromosome factory property in the future
    public void setTestSuiteExecution(boolean testSuiteExecution) {
        this.isTestSuiteExecution = testSuiteExecution;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        if (resetApp) {
            uiAbstractionLayer.resetApp();
        }

        // grant runtime permissions (read/write external storage) which are dropped after each reset
        MATE.log("Grant runtime permissions: "
                + Registry.getEnvironmentManager().grantRuntimePermissions(MATE.packageName));

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {
                if (!testCase.updateTestCase(selectAction(), String.valueOf(actionsCount))) {
                    return chromosome;
                }
            }
        } finally {
            if (!isTestSuiteExecution) {
                /*
                * If we deal with a test suite execution, the storing of coverage
                * and fitness data is handled by the AndroidSuiteRandomChromosomeFactory itself.
                 */
                FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
                CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
                CoverageUtils.logChromosomeCoverage(chromosome);
            }
            testCase.finish();
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
