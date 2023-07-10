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
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a chromosome factory that produces {@link TestCase}s consisting of a combination of
 * motif and regular ui actions.
 */
public class MotifAlphaChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * Controls the likelihood of motif actions, must be between 0 and 1 (inclusive).
     */
    private final float alpha;

    /**
     * Initialises the chromosome factory with the maximal number of actions and an alpha that
     * controls the likelihood of motif actions.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     * @param alpha A factor in [0,1] that controls the likelihood of motif action.
     */
    public MotifAlphaChromosomeFactory(int maxNumEvents, float alpha) {

        super(maxNumEvents);

        assert alpha >= 0.0 && alpha <= 1.0;
        this.alpha = alpha;
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

        // separate traces of restart operation from remaining traces produced by the actual actions
        storeAndLogActionCoverage(chromosome);

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {

                final Action newAction = selectAction();
                boolean stop = !testCase.updateTestCase(newAction, actionsCount);
                storeAndLogActionCoverage(chromosome);

                if (stop) {
                    MATE.log_warn("MotifAlphaChromosomeFactory: Action ( " + actionsCount + ") "
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

            testCase.finish();
        }
        return chromosome;
    }

    /**
     * Stores and logs the coverage of the individual actions.
     *
     * @param chromosome The current test case.
     */
    private void storeAndLogActionCoverage(final IChromosome<TestCase> chromosome) {

        // store coverage after every action
        CoverageUtils.storeActionCoverageData(chromosome);

        // log coverage of individual action
        CoverageUtils.logActionChromosomeCoverage(chromosome);

        // log intermediate coverage and coverage of chromosome
        CoverageUtils.logChromosomeCoverage(chromosome);
    }

    /**
     * Selects the next action to be executed. This can be either a motif action or a regular ui
     * action depending on the relative amount of motif actions and the weight factor specified by
     * {@link #alpha}.
     *
     * @return Returns the action to be performed next.
     */
    @Override
    protected Action selectAction() {

        /*
        * We employ the following formula for selecting a motif action instead of a regular UI action:
        *
        * P_motif = (#motif_actions / (#motif_actions + #ui_actions))^alpha, where 0 <= alpha <= 1
        * This implies that:
        * P_ui = 1 - P_motif
        *
        * A lower value for alpha favours the selection of a motif action, while with increasing
        * alpha this effect is diminished.
         */

        final double random = Randomness.getRnd().nextDouble();
        final List<UIAction> uiActions = uiAbstractionLayer.getExecutableUIActions();

        final List<UIAction> motifActions = uiActions.stream()
                .filter(uiAction -> uiAction instanceof MotifAction)
                .collect(Collectors.toList());

        if (motifActions.isEmpty()) { // P_motif = 0.0
            MATE.log_warn("No motif action applicable in current state!");
            return Randomness.randomElement(uiActions);
        } else {

            final int numberOfMotifActions = motifActions.size();
            final double relativeMotifActionAmount = (double) numberOfMotifActions / uiActions.size();

            /*
            * Assuming that the relative amount of motif actions in contrast to ui actions is in the
            * range of 5-20%, a low alpha favours the selection of a motif action, while with
            * increasing alpha this preference towards selecting a motif action is minimised. There
            * are two special cases for alpha:
            *
            * alpha = 0 -> Only motif actions are selected.
            * alpha = 1 -> Leads to a uniform distribution as employed in the default random exploration.
            *
            * One could alternatively use a self-regulating alpha by setting it as follows:
            * alpha = (#motif_actions / (#motif_actions + #ui_actions)
            *
             */
            final double probabilitySelectMotifAction
                    = Math.pow(relativeMotifActionAmount, alpha);

            if (random < probabilitySelectMotifAction) {
                return Randomness.randomElement(motifActions);
            } else {
                // select a plain UI action
                final List<UIAction> plainUIActions = uiActions.stream()
                        .filter(uiAction -> !(uiAction instanceof MotifAction))
                        .collect(Collectors.toList());

                return Randomness.randomElement(plainUIActions);
            }
        }
    }
}
