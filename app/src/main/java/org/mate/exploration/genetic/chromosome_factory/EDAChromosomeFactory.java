package org.mate.exploration.genetic.chromosome_factory;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.util.eda.ActionFitnessFunctionWrapper;
import org.mate.exploration.genetic.util.eda.IProbabilisticModel;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.TestCase;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.state.IScreenState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
        this.fitnessFunction = new ActionFitnessFunctionWrapper((IFitnessFunction<TestCase>) fitnessFunctions.get(0));
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
        this.fitnessFunction = new ActionFitnessFunctionWrapper((IFitnessFunction<TestCase>) fitnessFunctions.get(0));
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
            probabilisticModel.updatePositionImmutable(uiAbstractionLayer.getLastScreenState());
        }

        final TestCase testCase = TestCase.newInitializedTestCase();
        final Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        // Ignore (split off from first action) the traces produced by the reset of the AUT.
        storeFitnessData(chromosome);

        try {
            for (actionsCount = 0; !finishTestCase(); actionsCount++) {

                MATE.log("Current state: " + uiAbstractionLayer.getLastScreenState());
                MATE.log("Current state according to probabilistic model: " + probabilisticModel.getState());

                final Action nextAction = selectAction();

                MATE.log("Selected action: " + nextAction);

                if (nextAction instanceof UIAction // check that the ui action is actually applicable
                        && !uiAbstractionLayer.getExecutableUIActions().contains(nextAction)) {
                    MATE.log("Action not applicable in current state!");
                    return chromosome;
                }

                boolean stop = !testCase.updateTestCase(nextAction, actionsCount);
                storeFitnessData(chromosome);

                final IScreenState currentState = uiAbstractionLayer.getLastScreenState();
                probabilisticModel.updatePosition(testCase, nextAction, currentState);

                if (stop) {
                    return chromosome;
                }
            }
        } finally {

            // TODO: Check if the surrogate model can be integrated, I don't believe so without further changes.
            //  One needs to store coverage/fitness at MATE-Server after each action and do not cache it here.
            if (Properties.SURROGATE_MODEL()) {
                // update sequences + write traces to external storage
                SurrogateModel surrogateModel = (SurrogateModel) uiAbstractionLayer.getGuiModel();
                surrogateModel.updateTestCase(testCase);
            }

            /*
            * Storing coverage/fitness is already handled by storeFitnessData(), we only maintain
            * these calls to store coverage/fitness in case of a fault.
             */
            FitnessUtils.storeActionFitnessData(chromosome);
            CoverageUtils.storeActionCoverageData(chromosome);

            CoverageUtils.logChromosomeCoverage(chromosome);

            testCase.finish();
        }
        return chromosome;
    }

    /**
     * Stores the intermediate fitness of the chromosome, i.e. the fitness data associated with the
     * last executed action.
     *
     * @param chromosome The chromosome for which the fitness should be stored.
     */
    private void storeFitnessData(final IChromosome<TestCase> chromosome) {
        FitnessUtils.storeActionFitnessData(chromosome);
        fitnessFunction.recordCurrentActionFitness(chromosome);
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

        if (sortedProbabilities.size() == 1) {
            return sortedProbabilities.get(0).getKey();
        }

        double sum = 0;
        int index = 0;
        while (sum < randomNumber && index < sortedProbabilities.size()) {
            sum += sortedProbabilities.get(index).getValue();
            index++;
        }

        return sortedProbabilities.get(index - 1).getKey();
    }
}
