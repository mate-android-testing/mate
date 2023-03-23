package org.mate.exploration.genetic.mutation;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.TestCase;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

/**
 * Provides a cut point mutation function for {@link TestCase}s.
 */
public class CutPointMutationFunction implements IMutationFunction<TestCase> {

    /**
     * Provides primarily information about the current screen.
     */
    private final UIAbstractionLayer uiAbstractionLayer;

    /**
     * The maximal number of actions per test case.
     */
    private final int maxNumEvents;

    /**
     * Whether we deal with a test suite execution, i.e. whether the used chromosome factory
     * produces {@link org.mate.model.TestSuite}s or not.
     */
    private boolean isTestSuiteExecution = false;

    /**
     * Initialises the cut point mutation function.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public CutPointMutationFunction(int maxNumEvents) {
        this.uiAbstractionLayer = Registry.getUiAbstractionLayer();
        this.maxNumEvents = maxNumEvents;
    }

    // TODO: might be replaceable with chromosome factory property in the future
    /**
     * Defines whether we deal with a test suite execution or not.
     *
     * @param testSuiteExecution Indicates if we deal with a test suite execution or not.
     */
    public void setTestSuiteExecution(boolean testSuiteExecution) {
        this.isTestSuiteExecution = testSuiteExecution;
    }

    /**
     * Performs a cut point mutation. First, the given test case is split at a chosen cut point.
     * Then, the mutated test case is filled with the original actions up to the cut point and
     * from the cut point onwards with random actions.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<TestCase> mutate(IChromosome<TestCase> chromosome) {

        uiAbstractionLayer.resetApp();

        // choose an arbitrary cut point
        final int cutPoint = chooseCutPoint(chromosome.getValue());

        final TestCase testCase = chromosome.getValue();
        final TestCase mutant = TestCase.newInitializedTestCase();
        final IChromosome<TestCase> mutatedChromosome = new Chromosome<>(mutant);

        MATE.log_debug("Sequence length before mutation: " + testCase.getActionSequence().size());

        try {

            for (int i = 0; i < maxNumEvents; i++) {

                Action newAction;

                if (i < cutPoint) {

                    newAction = testCase.getActionSequence().get(i);

                    // Check that the ui action is still applicable.
                    if (newAction instanceof UIAction
                            && !uiAbstractionLayer.getExecutableUIActions().contains(newAction)) {
                        MATE.log_warn("CutPointMutationFunction: Action (" + i + ") "
                                + newAction.toShortString() + " not applicable!");
                        break; // Fill up with random actions.
                    }
                } else {
                    newAction = selectRandomAction();
                }

                if (!mutant.updateTestCase(newAction, i)) {
                    MATE.log_warn("CutPointMutationFunction: Action ( " + i + ") "
                            + newAction.toShortString() + " crashed or left AUT.");
                    return mutatedChromosome;
                }
            }

            // Fill up the remaining slots with random actions.
            final int currentTestCaseSize = mutant.getActionSequence().size();

            for (int i = currentTestCaseSize; i < maxNumEvents; ++i) {
                final Action newAction = selectRandomAction();
                if (!mutant.updateTestCase(newAction, i)) {
                    MATE.log_warn("CutPointMutationFunction: Action ( " + i + ") "
                            + newAction.toShortString() + " crashed or left AUT.");
                    return mutatedChromosome;
                }
            }
        } finally {

            if (Properties.SURROGATE_MODEL()) {
                // update sequences + write traces to external storage
                SurrogateModel surrogateModel
                        = (SurrogateModel) Registry.getUiAbstractionLayer().getGuiModel();
                surrogateModel.updateTestCase(mutant);
            }

            if (!isTestSuiteExecution) {
                /*
                 * If we deal with a test suite execution, the storing of coverage
                 * and fitness data is handled by the test suite mutation operator itself.
                 */
                FitnessUtils.storeTestCaseChromosomeFitness(mutatedChromosome);
                CoverageUtils.storeTestCaseChromosomeCoverage(mutatedChromosome);
                CoverageUtils.logChromosomeCoverage(mutatedChromosome);
            }

            mutant.finish();
            MATE.log_debug("Sequence length after mutation: " + mutant.getActionSequence().size());
        }

        return mutatedChromosome;
    }

    /**
     * Selects a random action applicable in the current state. Respects the intent probability.
     *
     * @return Returns the selected action.
     */
    private Action selectRandomAction() {

        final double random = Randomness.getRnd().nextDouble();

        if (Properties.USE_INTENT_ACTIONS() && random < Properties.RELATIVE_INTENT_AMOUNT()) {
            return Randomness.randomElement(uiAbstractionLayer.getExecutableIntentActions());
        } else {
            return Randomness.randomElement(uiAbstractionLayer.getExecutableUIActions());
        }
    }

    /**
     * Chooses a random cut point in the action sequence of the given test case.
     *
     * @param testCase The given test case.
     * @return Returns the selected cut point.
     */
    private int chooseCutPoint(TestCase testCase) {
        if (testCase.getActionSequence().isEmpty()) {
            MATE.log_warn("Choosing cut point from empty test case " + testCase + "!");
            return 0;
        } else {
            return Randomness.getRnd().nextInt(testCase.getActionSequence().size());
        }
    }
}
