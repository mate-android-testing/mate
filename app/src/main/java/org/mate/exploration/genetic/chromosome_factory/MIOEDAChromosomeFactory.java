package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.util.eda.IProbabilisticModel;
import org.mate.exploration.genetic.util.eda.ProbabilisticModelState;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.ChromosomeUtils;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.Utils;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A chromosome factory used in combination with an EDA approach. Executes an action and updates
 * the probabilistic model. Stores the fitness/coverage after each action. Do not use this factory
 * in combination with {@link org.mate.model.TestSuite}s, since we abuse the coverage/fitness storing
 * mechanism of test suites to store the coverage/fitness of individual actions.
 */
public class MIOEDAChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * The probabilistic model used in EDA.
     */
    private IProbabilisticModel<TestCase> probabilisticModel;

    /**
     * Records the traces on a per action-basis.
     */
    private final Map<String, Set<String>> tracesPerAction = new LinkedHashMap<>();

    /**
     * The state of a probabilistic model for each testing target, e.g., branch.
     */
    private List<ProbabilisticModelState<TestCase>> probabilisticModelStates;

    /**
     * Initialises the chromosome factory with the given properties.
     *
     * @param maxNumEvents       The maximal number of actions of a test.
     */
    public MIOEDAChromosomeFactory(int maxNumEvents) {
        super(maxNumEvents);
    }

    /**
     * Updates the probabilistic model that is used to sample new chromosomes.
     *
     * @param probabilisticModel The new probabilistic model.
     */
    public void setProbabilisticModel(final IProbabilisticModel<TestCase> probabilisticModel) {
        this.probabilisticModel = probabilisticModel;
    }

    /**
     * Sets the probabilistic models that should be updated when sampling a new chromosome.
     *
     * @param probabilisticModelStates The list of probabilistic models that should be updated.
     */
    public void setProbabilisticModelStates(List<ProbabilisticModelState<TestCase>> probabilisticModelStates) {
        this.probabilisticModelStates = probabilisticModelStates;
    }

    /**
     * Creates a new chromosome that wraps a test case consisting of actions that are sampled from
     * the underlying probabilistic model or random if no probabilistic model is provided. Note that
     * the chromosome is inherently executed.
     *
     * @return Returns the generated chromosome.
     */
    @Override
    public IChromosome<TestCase> createChromosome() {

        if (resetApp) {
            uiAbstractionLayer.resetApp();

            // Reset the model cursor to the root state for each probabilistic model.
            for (ProbabilisticModelState<TestCase> probabilisticModelState : probabilisticModelStates) {
                if (!probabilisticModelState.isCovered()) {
                    probabilisticModelState.getProbabilisticModel()
                            .resetPosition(uiAbstractionLayer.getLastScreenState());
                }
            }
        }

        final TestCase testCase = TestCase.newInitializedTestCase();
        final Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        // Ignore (split off from first action) the traces produced by the reset of the AUT.
        recordFitnessData(chromosome);

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {

                final Action nextAction = selectAction();
                boolean stop = !testCase.updateTestCase(nextAction, actionsCount);
                recordFitnessData(chromosome);

                final IScreenState currentState = uiAbstractionLayer.getLastScreenState();

                // Update the position of each probabilistic model.
                for (ProbabilisticModelState<TestCase> probabilisticModelState : probabilisticModelStates) {
                    if (!probabilisticModelState.isCovered()) {
                        probabilisticModelState.getProbabilisticModel()
                                .updatePosition(testCase, nextAction, currentState);
                    }
                }

                if (stop) {
                    MATE.log_warn("MIOEDAChromosomeFactory: Action ( " + actionsCount + ") "
                            + nextAction.toShortString() + " crashed or left AUT.");
                    return chromosome;
                }

                MATE.log_acc("Done with action #" + (actionsCount + 1));
            }
        } finally {

            // TODO: Check if the surrogate model can be integrated. This probably requires changes
            //  of the surrogate model, in particular to the intermediate trace storing functionality.

            // It is safe to terminate the exploration thread at this place.
            Utils.throwOnInterrupt();

            // We need to write out the recorded fitness data and inherently coverage data before we
            // can evaluate the fitness or coverage.
            storeFitnessData(chromosome);

            // We need to update the activity coverage manually here.
            CoverageUtils.updateTestCaseChromosomeActivityCoverage(chromosome,
                    testCase.getVisitedActivitiesOfApp());

            CoverageUtils.logChromosomeCoverage(chromosome);

            // Since the finish() method can be an expensive operation, we should terminate the
            // exploration thread upon receiving an interrupt ideally now or afterwards otherwise.
            Utils.throwOnInterrupt();
            testCase.finish();
            Utils.throwOnInterrupt();
        }
        MATE.log_acc("actionsCount: " + actionsCount);
        return chromosome;
    }

    /**
     * Records the fitness data and inherently coverage data on a per action-basis for the given chromosome.
     *
     * @param chromosome The given chromosome.
     */
    private void recordFitnessData(final IChromosome<TestCase> chromosome) {
        final String actionID = ChromosomeUtils.getActionEntityId(chromosome);
        final Set<String> traces = uiAbstractionLayer.getTraces();
        tracesPerAction.put(actionID, traces);
    }

    /**
     * Stores the recorded fitness data and inherently coverage data to disk for the given chromosome.
     *
     * @param chromosome The given chromosome.
     */
    private void storeFitnessData(final IChromosome<TestCase> chromosome) {

        FitnessUtils.storeActionFitnessData(chromosome, tracesPerAction);

        for (ProbabilisticModelState<TestCase> probabilisticModelState : probabilisticModelStates) {
            probabilisticModelState.getFitnessFunction().recordActionFitness(chromosome, tracesPerAction);
        }

        tracesPerAction.clear(); // clear traces for next chromosome
    }

    /**
     * Picks the next action based on roulette-wheel (fitness proportionate) selection.
     *
     * @return Returns the action to be executed next.
     */
    @Override
    protected Action selectAction() {

        if (probabilisticModel == null) {
            return super.selectAction(); // select random action
        }

        final Map<Action, Double> actionProbabilities = probabilisticModel.getActionProbabilities();

        final double randomNumber = Randomness.getRnd().nextDouble();
        final List<Map.Entry<Action, Double>> sortedProbabilities = actionProbabilities.entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .collect(Collectors.toList());

        Action chosenAction;

        if (sortedProbabilities.size() == 1) { // there is only a single action that can be taken
            chosenAction = sortedProbabilities.get(0).getKey();
        } else {

            double sum = 0.0;
            int index = 0;
            while (sum <= randomNumber && index < sortedProbabilities.size()) {
                sum += sortedProbabilities.get(index).getValue();
                index++;
            }

            chosenAction = sortedProbabilities.get(index - 1).getKey();
        }

        // Check that the chosen widget action is actually applicable. Since the 'BACK' action is a
        // plain UI action this action is inherently allowed.
        if (chosenAction instanceof WidgetAction
                && !uiAbstractionLayer.getExecutableUIActions().contains(chosenAction)) {
            MATE.log_warn("EDAChromosomeFactory: Action ( " + actionsCount + ") "
                    + chosenAction.toShortString() + " not applicable!");
            // TODO: Remove this candidate action from the current state of the probabilistic model?
            return super.selectAction(); // select random action
        }

        return chosenAction;
    }
}
