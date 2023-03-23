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
 * Provides a biased cut point mutation function for {@link TestCase}s. The cut point is chosen
 * near the end of the action sequence of the original test case such that most of the genetic
 * material is maintained.
 */
public class BiasedCutPointMutationFunction implements IMutationFunction<TestCase> {

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
    public BiasedCutPointMutationFunction(int maxNumEvents) {
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
    public IChromosome<TestCase> mutate(final IChromosome<TestCase> chromosome) {

        uiAbstractionLayer.resetApp();

        // chooses a cut point towards the end of the action sequence
        final int cutPoint = chooseCutPoint(chromosome.getValue());
        
        TestCase mutant = TestCase.newInitializedTestCase();
        IChromosome<TestCase> mutatedChromosome = new Chromosome<>(mutant);

        try {

            for (int i = 0; i < maxNumEvents; i++) {

                Action newAction;

                if (i < cutPoint) {

                    newAction = chromosome.getValue().getActionSequence().get(i);

                    if (newAction instanceof UIAction // check that the ui action is actually applicable
                            && !uiAbstractionLayer.getExecutableUIActions().contains(newAction)) {
                        break; // fill up with random actions
                    }
                } else {
                    newAction = Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
                }

                if (!mutant.updateTestCase(newAction, i)) {
                    return mutatedChromosome;
                }
            }

            // fill up the remaining slots with random actions
            final int currentTestCaseSize = mutant.getActionSequence().size();

            for (int i = currentTestCaseSize; i < maxNumEvents; ++i) {
                final Action newAction
                        = Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
                if (!mutant.updateTestCase(newAction, i)) {
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
        }

        return mutatedChromosome;
    }

    /**
     * Chooses a cut point biased towards the end in the action sequence of the given test case, i.e.
     * the cut point is chosen randomly in the last tenth.
     *
     * @param testCase The given test case.
     * @return Returns the selected cut point.
     */
    private int chooseCutPoint(final TestCase testCase) {
        if (testCase.getActionSequence().isEmpty()) {
            MATE.log_warn("Choosing cut point from empty test case " + testCase + "!");
            return 0;
        } else {
            final int startIndexOfLastTenth
                    = (int) Math.floor(testCase.getActionSequence().size() / (double) 10 * 9);
            return Randomness.getRandom(startIndexOfLastTenth, testCase.getActionSequence().size());
        }
    }
}
