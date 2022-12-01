package org.mate.exploration.genetic.mutation;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.PrimitiveAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a mutation function for {@link TestCase}s that replaces the individual actions by actions
 * of the same type, e.g. a {@link MotifAction} is replaced by a {@link MotifAction}. This mutation
 * is only applicable for {@link UIAction}s.
 */
public class TestCaseMutateActionParameters implements IMutationFunction<TestCase> {

    /**
     * Provides primarily information about the current screen.
     */
    private final UIAbstractionLayer uiAbstractionLayer;

    /**
     * Whether we deal with a test suite execution, i.e. whether the used chromosome factory
     * produces {@link org.mate.model.TestSuite}s or not.
     */
    private boolean isTestSuiteExecution = false;

    /**
     * Initialises the mutation function.
     */
    public TestCaseMutateActionParameters() {
        this.uiAbstractionLayer = Registry.getUiAbstractionLayer();
    }

    /**
     * Defines whether we deal with a test suite execution or not.
     *
     * @param testSuiteExecution Indicates if we deal with a test suite execution or not.
     */
    public void setTestSuiteExecution(boolean testSuiteExecution) {
        this.isTestSuiteExecution = testSuiteExecution;
    }

    /**
     * Replaces the individual actions of the test case chromosome with a different action of the
     * same type, e.g. a {@link MotifAction} is replaced by a {@link MotifAction}.
     *
     * @param chromosome The chromosome to be mutated.
     * @return Returns the mutated chromosome.
     */
    @Override
    public IChromosome<TestCase> mutate(IChromosome<TestCase> chromosome) {

        uiAbstractionLayer.resetApp();

        TestCase testCase = chromosome.getValue();
        TestCase mutant = TestCase.newInitializedTestCase();
        IChromosome<TestCase> mutatedChromosome = new Chromosome<>(mutant);

        try {
            for (int i = 0; i < testCase.getActionSequence().size(); i++) {
                final Action oldAction = chromosome.getValue().getActionSequence().get(i);
                if (oldAction instanceof UIAction) {
                    final UIAction newAction = mutate((UIAction) oldAction);
                    if (!mutant.updateTestCase(newAction, i)) {
                        break;
                    }
                } else {
                    if (!mutant.updateTestCase(oldAction, i)) {
                        break;
                    }
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
     * Mutates or actually replaces the given ui action.
     *
     * @param action The ui action that should be mutated.
     * @return Returns the mutated ui action.
     */
    private UIAction mutate(final UIAction action) {

        if (action instanceof MotifAction) {
            return mutate((MotifAction) action);
        } else if (action instanceof PrimitiveAction) {
            return mutate((PrimitiveAction) action);
        } else if (action instanceof WidgetAction) {
            return mutate((WidgetAction) action);
        } else {
            // replace with a random ui action
            final List<UIAction> candidateAction = uiAbstractionLayer
                    .getLastScreenState()
                    .getActions()
                    .stream()
                    .filter(a -> !action.equals(a)
                            && !(a instanceof MotifAction)
                            && !(a instanceof PrimitiveAction)
                            && !(a instanceof WidgetAction))
                    .collect(Collectors.toList());

            if (!candidateAction.isEmpty()) {
                return Randomness.randomElement(candidateAction);
            } else {
                return action;
            }
        }
    }

    /**
     * Replaces a motif action with a different motif action if available.
     */
    private UIAction mutate(final MotifAction action) {

        final List<MotifAction> candidateActions =
                uiAbstractionLayer.getLastScreenState().getMotifActions().stream()
                        .filter(motifAction -> !motifAction.equals(action))
                        .collect(Collectors.toList());

        if (!candidateActions.isEmpty()) {
            return Randomness.randomElement(candidateActions);
        } else {
            return action;
        }
    }

    /**
     * Replaces a primitive action with a different primitive action at the same coordinates. If the
     * given action refers to a insert text action, we choose a different text, otherwise we pick
     * a different action type.
     */
    private UIAction mutate(final PrimitiveAction action) {

        if (action.getActionType() == ActionType.TYPE_TEXT) {
            return new PrimitiveAction(action.getX(), action.getY(), ActionType.TYPE_TEXT,
                    action.getActivityName());
        } else {
            // Do not replace the action type with action type TYPE_TEXT, else we will eventually
            // end up with all actions being primitive TYPE_TEXT actions.
            final List<ActionType> candidates = Arrays.stream(ActionType.primitiveActionTypes)
                    .filter(actionType -> actionType != action.getActionType())
                    .filter(actionType -> actionType != ActionType.TYPE_TEXT)
                    .collect(Collectors.toList());
            final ActionType newActionType = Randomness.randomElement(candidates);
            return new PrimitiveAction(action.getX(), action.getY(), newActionType, action.getActivityName());
        }
    }

    /**
     * Replaces a widget action with a different widget action. Tries first to choose another widget
     * on which the same type of action can be applied. If this fails, a different action on the
     * same widget is chosen. If no such action exists, we choose a random widget action.
     */
    private UIAction mutate(final WidgetAction action) {

        final ActionType actionType = action.getActionType();
        final Widget widget = action.getWidget();

        final List<WidgetAction> widgetActions
                = uiAbstractionLayer.getLastScreenState().getWidgetActions().stream()
                .filter(widgetAction -> !widgetAction.equals(action))
                .collect(Collectors.toList());

        if (widgetActions.isEmpty()) { // no other widget action available -> leave action unchanged
            return action;
        }

        // try to find a widget on which the same kind of action, e.g. CLICK, can be applied.
        final List<WidgetAction> differentWidgetSameAction = widgetActions.stream()
                .filter(widgetAction -> actionType.equals(widgetAction.getActionType())
                        && !widget.equals(widgetAction.getWidget()))
                .collect(Collectors.toList());

        if (!differentWidgetSameAction.isEmpty()) {
            return Randomness.randomElement(differentWidgetSameAction);
        }

        // try to apply a different action on the same widget
        final List<WidgetAction> sameWidgetDifferentAction = widgetActions.stream()
                .filter(widgetAction -> widget.equals(widgetAction.getWidget()))
                .collect(Collectors.toList());

        if (!sameWidgetDifferentAction.isEmpty()) {
            return Randomness.randomElement(differentWidgetSameAction);
        }

        return Randomness.randomElement(widgetActions);
    }
}

