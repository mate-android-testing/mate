package org.mate.exploration.genetic.mutation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.intent.BundleGenerator;
import org.mate.exploration.intent.DataUriGenerator;
import org.mate.exploration.intent.IntentProvider;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.intent.IntentAction;
import org.mate.interaction.action.intent.IntentBasedAction;
import org.mate.interaction.action.intent.SystemAction;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides a mutation function for {@link TestCase}s that replaces the individual actions by actions
 * of the same type, e.g. a {@link MotifAction} is replaced by a {@link MotifAction}.
 */
public class TestCaseActionParametersMutationFunction implements IMutationFunction<TestCase> {

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
     * The maximal number of actions per test case.
     */
    private final int maxNumEvents;

    /**
     * Enables access to the intent generation.
     */
    private static final IntentProvider intentProvider = new IntentProvider();

    /**
     * Initialises the mutation function.
     */
    public TestCaseActionParametersMutationFunction(final int maxNumEvents) {
        this.uiAbstractionLayer = Registry.getUiAbstractionLayer();
        this.maxNumEvents = maxNumEvents;
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

        final TestCase testCase = chromosome.getValue();
        final TestCase mutant = TestCase.newInitializedTestCase();
        final IChromosome<TestCase> mutatedChromosome = new Chromosome<>(mutant);

        MATE.log_debug("Sequence length before mutation: " + testCase.getActionSequence().size());

        try {

            for (int i = 0; i < testCase.getActionSequence().size(); i++) {

                final Action oldAction = testCase.getActionSequence().get(i);
                final Action newAction = mutate(oldAction);

                // Check that the ui action is still applicable.
                if (newAction instanceof UIAction
                        && !uiAbstractionLayer.getExecutableUIActions().contains(newAction)) {
                    MATE.log_warn("TestCaseActionParametersMutationFunction: "
                            + "Action (" + i + ") " + newAction.toShortString() + " not applicable!");
                    break; // Fill up with random actions.
                } else if (!mutant.updateTestCase(newAction, i)) {
                    MATE.log_warn("TestCaseActionParametersMutationFunction: "
                            + "Action ( " + i + ") " + newAction.toShortString()
                            + " crashed or left AUT.");
                    return mutatedChromosome;
                }
            }

            // Fill up the remaining slots with random actions.
            final int currentTestCaseSize = mutant.getActionSequence().size();

            for (int i = currentTestCaseSize; i < maxNumEvents; ++i) {
                final Action newAction = selectRandomAction();
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
     * Mutates the given intent action.
     *
     * @param action The action to be mutated.
     * @return Returns the mutated intent action.
     */
    private IntentAction mutate(final IntentAction action) {
        if (action instanceof IntentBasedAction) {
            return mutate((IntentBasedAction) action);
        } else if (action instanceof SystemAction) {
            return mutate((SystemAction) action);
        } else {
            throw new UnsupportedOperationException("Unknown action type: " + action.getClass());
        }
    }

    /**
     * Replaces the categories of the given intent with the supplied categories. Unfortunately,
     * you can't call clear() and addAll() directly, because internally {@code null} is used when
     * the set of categories is empty.
     *
     * @param intent The given intent.
     * @param categories The new categories for the intent.
     */
    private void replaceCategories(Intent intent, final Set<String> categories) {

        final Set<String> oldCategories = intent.getCategories();

        if (oldCategories != null) {
            for (final String category : oldCategories) {
                intent.removeCategory(category);
            }
        }

        for (final String category : categories) {
            intent.addCategory(category);
        }
    }

    /**
     * Mutates the given intent action by replacing individual attributes like action, categories,
     * data uri and extras.
     *
     * @param action The intent action to be mutated.
     * @return Returns the mutated intent action.
     */
    private IntentAction mutate(final IntentBasedAction action) {

        // TODO: Mutate each attribute only with a probability of 1/2?

        // mutate action
        if (action.getIntentFilter().hasAction()
                && action.getIntentFilter().getActions().size() > 1) {
            final Set<String> actions = action.getIntentFilter().getActions();
            final String oldAction = action.getIntent().getAction();
            String randomAction = Randomness.randomElement(actions);
            while (randomAction.equals(oldAction)) {
                randomAction = Randomness.randomElement(actions);
            }
            action.getIntent().setAction(randomAction);
        }

        // mutate categories
        if (action.getIntentFilter().hasCategory()
                && action.getIntentFilter().getCategories().size() > 1) {
            final Set<String> categories
                    = Randomness.randomSubset(action.getIntentFilter().getCategories());
            replaceCategories(action.getIntent(), categories);
        }

        // mutate data uri
        if (action.getIntentFilter().hasData()) {
            final Uri uri = DataUriGenerator.generateRandomUri(action.getIntentFilter().getData());
            if (uri != null) {
                action.getIntent().setData(uri);
            }
        }

        // mutate extras
        if (action.getComponent().hasExtras()) {
            final Bundle extras = BundleGenerator.generateRandomBundle(action.getComponent());
            action.getIntent().putExtras(extras);
        }

        return action;
    }

    /**
     * Mutates the given system action by replacing the system event if possible.
     *
     * @param action The system action to be mutated.
     * @return Returns the mutated system action.
     */
    private IntentAction mutate(final SystemAction action) {

        // mutate action
        if (action.getIntentFilter().hasAction()
                && action.getIntentFilter().getActions().size() > 1) {
            final Set<String> actions = action.getIntentFilter().getActions();
            final String oldAction = action.getAction();
            String randomAction = Randomness.randomElement(actions);
            while (randomAction.equals(oldAction)) {
                randomAction = Randomness.randomElement(actions);
            }

            if (intentProvider.describesSystemEvent(randomAction)) {
                // only if the new action also refers to a system event
                action.setAction(randomAction);
            }
        }

        return action;
    }

    /**
     * Mutates or actually replaces the given action.
     *
     * @param action The action that should be mutated.
     * @return Returns the mutated action.
     */
    private Action mutate(final Action action) {

        if (action instanceof IntentAction) {
            return mutate((IntentAction) action);
        } else if (action instanceof MotifAction) {
            return mutate((MotifAction) action);
        } else if (action instanceof PrimitiveAction) {
            return mutate((PrimitiveAction) action);
        } else if (action instanceof WidgetAction) {
            return mutate((WidgetAction) action);
        } else {
            // replace with a random ui action
            final List<UIAction> candidateActions = uiAbstractionLayer
                    .getLastScreenState()
                    .getUIActions()
                    .stream()
                    .filter(a -> !action.equals(a)
                            && !(a instanceof MotifAction)
                            && !(a instanceof PrimitiveAction)
                            && !(a instanceof WidgetAction)
                            // prevent possible shortening of action sequence
                            && a.getActionType() != ActionType.HOME
                            && a.getActionType() != ActionType.BACK)
                    .collect(Collectors.toList());

            if (!candidateActions.isEmpty()) {
                return Randomness.randomElement(candidateActions);
            } else {
                return action;
            }
        }
    }

    /**
     * Replaces a motif action with a different motif action if available.
     */
    private UIAction mutate(final MotifAction action) {

        // TODO: Make a more fine-grained mutation depending on the concrete motif action.

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
            return Randomness.randomElement(sameWidgetDifferentAction);
        }

        return Randomness.randomElement(widgetActions);
    }
}

