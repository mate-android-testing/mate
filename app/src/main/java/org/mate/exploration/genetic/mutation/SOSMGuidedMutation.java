package org.mate.exploration.genetic.mutation;

import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.TestCase;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;
import org.mate.model.fsm.sosm.ActionsAndOpinion;
import org.mate.model.fsm.sosm.SOSMModel;
import org.mate.model.fsm.sosm.Trace;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.Tuple;
import org.mate.utils.coverage.CoverageUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a guided mutation function for {@link TestCase}s based on the SOSM model.
 */
public class SOSMGuidedMutation implements ISOSMMutationFunction {

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
     * The underlying SOSM model.
     */
    private final SOSMModel sosmModel = (SOSMModel) Registry.getUiAbstractionLayer().getGuiModel();

    /**
     * Initialises the guided mutation function.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public SOSMGuidedMutation(int maxNumEvents) {
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
     * from the cut point onwards with actions based on the underlying subjective opinion.
     *
     * @param chromosome The given test case chromosome that should be mutated.
     * @param trace Describes which transitions have been taken by the test case chromosome.
     * @return Returns the mutated chromosome along with the generated trace.
     */
    @Override
    public Tuple<IChromosome<TestCase>, Trace> mutate(final IChromosome<TestCase> chromosome,
                                                      final Trace trace) {

        uiAbstractionLayer.resetApp();

        // Record the taken transitions of the newly generated offspring.
        sosmModel.resetRecordedTransitions();

        final TestCase mutant = TestCase.newInitializedTestCase();
        final IChromosome<TestCase> mutatedChromosome = new Chromosome<>(mutant);
        final List<Action> actionSequence = chromosome.getValue().getActionSequence();
        final int cutPoint = chooseCutPoint(trace);

        try {
            for (int i = 0; i < maxNumEvents; i++) {
                final Action newAction
                        = chooseNextAction(actionSequence, cutPoint,
                        sosmModel.getCurrentState(), i);
                if (!mutant.updateTestCase(newAction, i)) {
                    break;
                }
            }
        } finally {

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

        return new Tuple<>(mutatedChromosome, new Trace(sosmModel.getRecordedTransitions()));
    }

    /**
     * Determines a cut point, i.e. the point from which on actions are selected based on the
     * subjective opinion. Considers the uncertainties along the taken path and performs a roulette
     * wheel selection.
     *
     * @param trace Describes the transitions taken by the test case.
     * @return Returns the chosen cut point.
     */
    private int chooseCutPoint(final Trace trace) {

        if (trace.isEmpty()) {
            return 0;
        }

        final List<Transition> transitions = trace.getTransitions();
        final double[] uncertainties = new double[transitions.size() + 1];

        // Perform a roulette-wheel selection over the uncertainties along the taken transitions.
        double sum = 0.0;

        for (int i = 0; i < transitions.size(); ++i) {
            final State state = transitions.get(i).getSource();
            final ActionsAndOpinion actionsAndOpinion = sosmModel.getActionsAndOpinionOn(state);
            final double uncertainty = actionsAndOpinion != null
                    ? actionsAndOpinion.getUncertainty() : 0.0;
            uncertainties[i] = uncertainty;
            sum += uncertainty;
        }

        final State last = transitions.get(trace.size() - 1).getTarget();
        final ActionsAndOpinion actionsAndOpinion = sosmModel.getActionsAndOpinionOn(last);
        final double uncertainty = actionsAndOpinion != null
                ? actionsAndOpinion.getUncertainty() : 0.0;
        uncertainties[trace.size()] = uncertainty;
        sum += uncertainty;

        if (sum == 0.0) {
            return Randomness.getRnd().nextInt(transitions.size() + 1);
        }

        final double chosen = Randomness.getRandom(0.0, sum);
        double start = 0;
        for (int i = 0; i < uncertainties.length; ++i) {
            final double end = start + uncertainties[i];
            if (chosen < end)
                return i;
            start = end;
        }

        throw new AssertionError("Unreachable");
    }

    /**
     * Chooses the action to be executed next.
     *
     * @param actionSequence The action sequence of the original chromosome.
     * @param cutPoint The chosen cut point.
     * @param state The current state according to the SOSM.
     * @param actionCount The action id of the next action.
     * @return Returns the chosen action.
     */
    private Action chooseNextAction(final List<? extends Action> actionSequence,
                                    final int cutPoint, final State state, final int actionCount) {

        if (actionCount + 1 >= cutPoint) {
            // Consider the subjective opinions for selecting an action from the cut point onwards.
            return chooseNextActionBasedOnState(state);
        }

        final Action action = actionSequence.get(actionCount);

        // Apply UI action only if executable in current state, otherwise pick a random action.
        if (action instanceof UIAction
                && uiAbstractionLayer.getExecutableUIActions().contains(action)) {
            return action;
        } else {
            return Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
        }
    }

    /**
     * Chooses a new action based on the subjective opinion associated with the current state.
     *
     * @param state The current state according to the SOSM.
     * @return Returns the chosen action.
     */
    private Action chooseNextActionBasedOnState(final State state) {

        ActionsAndOpinion actionsAndOpinion = sosmModel.getActionsAndOpinionOn(state);

        if (actionsAndOpinion == null) {
            return Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
        }

        // If there is a high uncertainty associated with the current state, we favour a random
        // action in order to increase belief over time.
        if (Randomness.getRnd().nextDouble() < actionsAndOpinion.getUncertainty()) {
            return Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
        }

        final List<Tuple<Double, Action>> candidates
                = actionsAndOpinion
                .getActions()
                .stream()
                .filter(uiAbstractionLayer.getExecutableActions()::contains)
                .map(action -> new Tuple<>(actionsAndOpinion.opinionOfAction(action)
                        .getDisbelief(), action))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
        }

        // Perform a roulette-wheel selection over the disbelief of the candidate actions.
        final double sum = candidates.stream().mapToDouble(Tuple::getX).sum();

        if (sum <= 0.0) {
            return Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
        }

        final double chosen = Randomness.getRandom(0.0, sum);
        double start = 0;

        for (Tuple<Double, Action> candidate : candidates) {
            final double end = start + candidate.getX();
            if (chosen < end)
                return candidate.getY();

            start = end;
        }

        throw new AssertionError("Unreachable");
    }
}
