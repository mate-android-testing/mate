package org.mate.exploration.genetic.chromosome_factory;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

/**
 * Provides a chromosome factory that generates {@link TestCase}s consisting of random
 * {@link org.mate.interaction.action.ui.UIAction}s.
 */
public class AndroidRandomChromosomeFactory implements IChromosomeFactory<TestCase> {

    /**
     * A reference to the ui abstraction layer.
     */
    protected final UIAbstractionLayer uiAbstractionLayer;

    /**
     * The maximal number of actions per test case.
     */
    protected final int maxNumEvents;

    /**
     * Whether to reset the AUT before creating a new chromosome (test case).
     */
    protected final boolean resetApp;

    /**
     * Whether this chromosome factory is used within a test suite chromosome factory.
     */
    protected boolean isTestSuiteExecution;

    /**
     * The current action count.
     */
    protected int actionsCount;

    /**
     * Initialises a new chromosome factory that is capable of generating random {@link TestCase}s.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public AndroidRandomChromosomeFactory(int maxNumEvents) {
        this(true, maxNumEvents);
    }

    /**
     * Initialises a new chromosome factory that is capable of generating random {@link TestCase}s.
     *
     * @param resetApp Whether to reset the AUT before creating a new chromosome (test case).
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public AndroidRandomChromosomeFactory(boolean resetApp, int maxNumEvents) {
        this.uiAbstractionLayer = Registry.getUiAbstractionLayer();
        this.maxNumEvents = maxNumEvents;
        this.resetApp = resetApp;
        isTestSuiteExecution = false;
        actionsCount = 0;
    }

    // TODO: might be replaceable with chromosome factory property in the future
    /**
     * Defines whether this chromosome factory is used within a test suite chromosome factory.
     *
     * @param testSuiteExecution Whether we deal with a test suite execution.
     */
    public void setTestSuiteExecution(boolean testSuiteExecution) {
        this.isTestSuiteExecution = testSuiteExecution;
    }

    /**
     * Creates a new chromosome that wraps a test case consisting of random actions. Note that
     * the chromosome is inherently executed.
     *
     * @return Returns the generated chromosome.
     */
    @Override
    public IChromosome<TestCase> createChromosome() {

        if (resetApp) {
            uiAbstractionLayer.resetApp();
        }

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {
                if (!testCase.updateTestCase(selectAction(), actionsCount)) {
                    return chromosome;
                }
            }
        } finally {

            if (Properties.SURROGATE_MODEL()) {
                // update sequences + write traces to external storage
                SurrogateModel surrogateModel = (SurrogateModel) uiAbstractionLayer.getGuiModel();
                surrogateModel.updateTestCase(testCase);
            }

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

    /**
     * Defines when the test case creation, i.e. the filling with actions, should be stopped.
     *
     * @return Returns {@code true} when the test case creation should be stopped,
     *          otherwise {@code false} is returned.
     */
    protected boolean finishTestCase() {
        return actionsCount >= maxNumEvents;
    }

    /**
     * Selects a random action.
     *
     * @return Returns the randomly selected action.
     */
    protected Action selectAction() {
        return Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
    }
}
