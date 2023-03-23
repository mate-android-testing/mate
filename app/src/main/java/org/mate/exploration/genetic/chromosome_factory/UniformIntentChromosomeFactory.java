package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

/**
 * Provides a chromosome factory that produces {@link TestCase}s consisting of a combination of
 * {@link org.mate.interaction.action.ui.UIAction}, {@link org.mate.interaction.action.intent.IntentBasedAction}
 * and {@link org.mate.interaction.action.intent.SystemAction} actions. In contrast to
 * {@link IntentChromosomeFactory}, an intent is uniformly selected from the pre-computed intent
 * actions.
 */
public class UniformIntentChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * The relative amount of intent and system actions.
     */
    private final float relativeIntentAmount;

    /**
     * Initialises the chromosome factory with the maximal number of actions and the probability
     * for generating an intent-based or system action instead of a ui action.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     * @param relativeIntentAmount The probability in [0,1] for generating an intent-based or
     *                             system action.
     */
    public UniformIntentChromosomeFactory(int maxNumEvents, float relativeIntentAmount) {

        super(maxNumEvents);

        assert relativeIntentAmount >= 0.0 && relativeIntentAmount <= 1.0;
        this.relativeIntentAmount = relativeIntentAmount;
    }

    /**
     * Creates a new chromosome wrapping a test case which in turn consists of a combination of
     * intent-based, system and ui actions.
     *
     * @return Returns the generated chromosome.
     */
    @Override
    public IChromosome<TestCase> createChromosome() {

        if (resetApp) {
            uiAbstractionLayer.resetApp();
        }

        // TODO: If we can ensure that sdcard files are not touched by the app, then pushing
        //  those files is redundant and we could do this once before creating the first chromosome
        // push dummy files onto sd card
        MATE.log("Pushing custom media files: "
                + Registry.getEnvironmentManager().pushDummyFiles());

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (int i = 0; i < maxNumEvents; i++) {
                if (!testCase.updateTestCase(selectAction(), i)) {
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
     * Selects the next action to be executed. This can be either an an intent-based action,
     * a system event notification or a ui action depending on the probability specified by
     * {@link #relativeIntentAmount}.
     *
     * @return Returns the action to be performed next.
     */
    @Override
    protected Action selectAction() {

        final double random = Randomness.getRnd().nextDouble();

        if (random < relativeIntentAmount) {
            return Randomness.randomElement(uiAbstractionLayer.getExecutableIntentActions());
        } else {
            return Randomness.randomElement(uiAbstractionLayer.getExecutableUIActions());
        }
    }
}
