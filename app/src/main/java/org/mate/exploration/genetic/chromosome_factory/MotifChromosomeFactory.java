package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.TestCase;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a chromosome factory that produces {@link TestCase}s consisting of a combination of
 * motif and regular ui actions.
 */
public class MotifChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * The relative amount of motif actions in contrast to regular ui actions.
     */
    private final float relativeMotifActionAmount;

    /**
     * Initialises the chromosome factory with the maximal number of actions and the probability
     * for generating a motif action instead of a ui action.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     * @param relativeMotifActionAmount The probability in [0,1] for generating a motif action in
     *              favor of a regular ui action.
     */
    public MotifChromosomeFactory(int maxNumEvents, float relativeMotifActionAmount) {

        super(maxNumEvents);

        assert relativeMotifActionAmount >= 0.0 && relativeMotifActionAmount <= 1.0;
        this.relativeMotifActionAmount = relativeMotifActionAmount;
    }

    /**
     * Creates a new chromosome wrapping a test case which in turn consists of a combination of
     * motif and ui actions.
     *
     * @return Returns the generated chromosome.
     */
    @Override
    public IChromosome<TestCase> createChromosome() {

        if (resetApp) {
            uiAbstractionLayer.resetApp();
        }

        final TestCase testCase = TestCase.newInitializedTestCase();
        final Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {

                final Action newAction = selectAction();

                if (!testCase.updateTestCase(newAction, actionsCount)) {
                    MATE.log_warn("MotifChromosomeFactory: Action ( " + actionsCount + ") "
                            + newAction.toShortString() + " crashed or left AUT.");
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
     * Selects the next action to be executed. This can be either a motif action or a regular ui
     * action depending on the probability specified by {@link #relativeMotifActionAmount}.
     *
     * @return Returns the action to be performed next.
     */
    @Override
    protected Action selectAction() {

        final double random = Randomness.getRnd().nextDouble();

        final List<UIAction> uiActions = uiAbstractionLayer.getExecutableUIActions();

        if (random < relativeMotifActionAmount) {

            // select a motif action if applicable
            final List<UIAction> motifActions = uiActions.stream()
                    .filter(uiAction -> uiAction instanceof MotifAction)
                    .collect(Collectors.toList());

            if (!motifActions.isEmpty()) {
                return Randomness.randomElement(motifActions);
            } else {
                MATE.log_warn("No motif action applicable in current state!");
                return Randomness.randomElement(uiActions);
            }

        } else {

            // select a plain UI action
            final List<UIAction> plainUIActions = uiActions.stream()
                    .filter(uiAction -> !(uiAction instanceof MotifAction))
                    .collect(Collectors.toList());

            return Randomness.randomElement(plainUIActions);
        }
    }
}
