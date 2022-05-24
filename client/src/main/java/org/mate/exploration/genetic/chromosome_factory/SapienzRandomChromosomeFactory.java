package org.mate.exploration.genetic.chromosome_factory;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.MotifAction;
import org.mate.commons.interaction.action.ui.PrimitiveAction;
import org.mate.commons.utils.Randomness;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.TestCase;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.utils.FitnessUtils;
import org.mate.utils.coverage.CoverageUtils;

/**
 * Provides a chromosome factory as described in the Sapienz paper, i.e. it generates test cases
 * consisting of atomic (primitive) and motif genes. Requires that the property
 * {@link Properties#WIDGET_BASED_ACTIONS()} is set to {@code false}.
 */
public class SapienzRandomChromosomeFactory implements IChromosomeFactory<TestCase> {

    /**
     * A reference to the ui abstraction layer, this allows to reset the AUT.
     */
    private final UIAbstractionLayer uiAbstractionLayer;

    /**
     * The maximal number of actions per test case.
     */
    private final int maxNumEvents;

    /**
     * Whether we want to reset the app before each test case.
     */
    private final boolean resetApp;

    /**
     * Whether this chromosome factory is used within a test suite chromosome factory.
     */
    private boolean isTestSuiteExecution;

    /**
     * The current action counter.
     */
    private int actionsCount;

    /**
     * Initialises a new chromosome factory with the given number of maximal events per chromosome.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public SapienzRandomChromosomeFactory(int maxNumEvents) {
        this(true, maxNumEvents);
    }

    /**
     * Initialises a new chromosome factory with the given number of maximal events per chromosome
     * and whether the AUT should be reset prior to creating a new chromosome.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public SapienzRandomChromosomeFactory(boolean resetApp, int maxNumEvents) {
        this.uiAbstractionLayer = Registry.getUiAbstractionLayer();
        this.maxNumEvents = maxNumEvents;
        this.resetApp = resetApp;
        isTestSuiteExecution = false;
        actionsCount = 0;
    }

    /**
     * Informs the chromosome factory whether we deal with a test suite execution or not.
     *
     * @param testSuiteExecution Whether we deal with a test suite execution or not.
     */
    public void setTestSuiteExecution(boolean testSuiteExecution) {
        this.isTestSuiteExecution = testSuiteExecution;
    }

    /**
     * Generates a new chromosome that consists of a mix of atomic (primitive) and motif genes.
     *
     * @return Returns the newly generated chromosome.
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
                 * and fitness data is handled by the SapienzSuiteRandomChromosomeFactory itself.
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
    private boolean finishTestCase() {
        return actionsCount >= maxNumEvents;
    }

    /**
     * Selects either a random primitive or motif action.
     *
     * @return Returns the randomly selected action.
     */
    private Action selectAction() {
        final double random = Randomness.getRnd().nextDouble();
        if (random < 0.5) {
            return PrimitiveAction.randomAction(
                    Registry.getUiAbstractionLayer().getCurrentActivity(),
                    Registry.getUiAbstractionLayer().getScreenWidth(),
                    Registry.getUiAbstractionLayer().getScreenHeight());
        } else {
            return MotifAction.randomAction(Registry.getUiAbstractionLayer().getCurrentActivity());
        }
    }
}
