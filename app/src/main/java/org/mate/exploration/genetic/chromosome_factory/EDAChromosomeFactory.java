package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.ActionFitnessFunctionWrapper;
import org.mate.exploration.genetic.fitness.IActionFitnessFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.util.eda.IProbabilisticModel;
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
public class EDAChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * The probabilistic model used in EDA.
     */
    private final IProbabilisticModel<TestCase> probabilisticModel;

    /**
     * Wraps the underlying fitness function such that we can store and retrieve the fitness after
     * individual actions.
     */
    private final ActionFitnessFunctionWrapper fitnessFunction;

    /**
     * Records the traces on a per action-basis.
     */
    private final Map<String, Set<String>> tracesPerAction = new LinkedHashMap<>();

    /**
     * Initialises the chromosome factory with the given properties.
     *
     * @param maxNumEvents The maximal number of actions of a test.
     * @param probabilisticModel The probabilistic model used in EDA.
     * @param fitnessFunctions The list of fitness functions. Right now EDA only supports a single
     *                          fitness function!
     * @param <T> The type wrapped by the chromosomes, must be a test case here.
     */
    public <T> EDAChromosomeFactory(int maxNumEvents,
                                IProbabilisticModel<T> probabilisticModel,
                                List<IFitnessFunction<T>> fitnessFunctions) {
        super(maxNumEvents);
        assert fitnessFunctions.size() == 1;
        this.probabilisticModel = (IProbabilisticModel<TestCase>) probabilisticModel;
        this.fitnessFunction
                = new ActionFitnessFunctionWrapper((IActionFitnessFunction<TestCase>) fitnessFunctions.get(0));
    }

    /**
     * Initialises the chromosome factory with the given properties.
     *
     * @param resetApp Whether to reset the AUT before initialising a new test case.
     * @param maxNumEvents The maximal number of actions of a test.
     * @param probabilisticModel The probabilistic model used in EDA.
     * @param fitnessFunctions The list of fitness functions. Right now EDA only supports a single
     *                          fitness function!
     * @param <T> The type wrapped by the chromosomes, must be a test case here.
     */
    public <T> EDAChromosomeFactory(boolean resetApp, int maxNumEvents,
                                IProbabilisticModel<T> probabilisticModel,
                                List<IFitnessFunction<T>> fitnessFunctions) {
        super(resetApp, maxNumEvents);
        assert fitnessFunctions.size() == 1;
        this.probabilisticModel = (IProbabilisticModel<TestCase>) probabilisticModel;
        this.fitnessFunction
                = new ActionFitnessFunctionWrapper((IActionFitnessFunction<TestCase>) fitnessFunctions.get(0));
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

            // reset the model cursor to the root state
            probabilisticModel.resetPosition(uiAbstractionLayer.getLastScreenState());
        }

        final TestCase testCase = TestCase.newInitializedTestCase();
        final Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        // Ignore (split off from first action) the traces produced by the reset of the AUT.
        recordFitnessData(chromosome);

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {

                if (!probabilisticModel.getState().equals(uiAbstractionLayer.getLastScreenState())) {
                    throw new IllegalStateException("Probabilistic model is not synced to current state!");
                }

                final Action nextAction = selectAction();
                boolean stop = !testCase.updateTestCase(nextAction, actionsCount);
                recordFitnessData(chromosome);

                final IScreenState currentState = uiAbstractionLayer.getLastScreenState();
                probabilisticModel.updatePosition(testCase, nextAction, currentState);

                if (stop) {
                    MATE.log_warn("EDAChromosomeFactory: Action ( " + actionsCount + ") "
                            + nextAction.toShortString() + " crashed or left AUT.");
                    return chromosome;
                }
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
        return chromosome;
    }

    /**
     * Stores the intermediate coverage and fitness of the chromosome, i.e. the coverage/fitness data
     * associated with the last executed action.
     *
     * NOTE: This implementation has been replaced in favour of a faster implementation that directly
     * retrieves the traces (coverage/fitness data) from the external storage and stores them to disk
     * in one pass upon test case completion, see {@link #recordFitnessData(IChromosome)} and
     * {@link #storeFitnessData(IChromosome)}.
     *
     * @param chromosome The chromosome for which coverage and fitness should be stored.
     */
    @SuppressWarnings("unused")
    private void storeCoverageAndFitnessData(final IChromosome<TestCase> chromosome) {
        CoverageUtils.storeActionCoverageData(chromosome);
        FitnessUtils.storeActionFitnessData(chromosome);
        fitnessFunction.recordCurrentActionFitness(chromosome);
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
        fitnessFunction.recordActionFitness(chromosome, tracesPerAction);
        tracesPerAction.clear(); // clear traces for next chromosome
    }

    /**
     * Picks the next action based on roulette-wheel (fitness proportionate) selection.
     *
     * @return Returns the action to be executed next.
     */
    @Override
    protected Action selectAction() {

        final Map<Action, Double> actionProbabilities = probabilisticModel.getActionProbabilities();

        final double randomNumber = Randomness.getRnd().nextDouble();
        final List<Map.Entry<Action, Double>> sortedProbabilities = actionProbabilities.entrySet().stream()
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .collect(Collectors.toList());

        Action chosenAction;

        if (sortedProbabilities.isEmpty()) {
            throw new IllegalStateException("State without any defined action probabilities!");
        }

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
