package org.mate.exploration.genetic.chromosome_factory;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.interaction.action.ui.WidgetAction;
import org.mate.model.TestCase;
import org.mate.commons.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This factory generates test cases where the individual actions are weighted and selected
 * according to the approach used in Stoat (https://tingsu.github.io/files/fse17-stoat.pdf),
 * see section 3.2.
 */
public class HeuristicalChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * Stores how many times an action was executed so far.
     */
    private final Map<Action, Integer> executionCounter = new HashMap<>();

    /**
     * The list of already visited widgets.
     */
    private final List<String> visitedWidgetIds = new ArrayList<>();

    /**
     * Stores the number of unvisited widgets after executing an action.
     */
    private final Map<Action, Integer> unvisitedChildWidgetCounter = new HashMap<>();

    /**
     * Stores a list of actions that lead to discovery of a widget.
     */
    private final Map<String, Set<Action>> actionsPrecedingWidget = new HashMap<>();

    /**
     * The hyperparameters as described in section 3.2 of the Stoat paper.
     */
    private final double alpha;
    private final double beta;
    private final double gamma;

    private Action previousAction = null;

    /**
     * Initialises a new chromosome factory that generates test cases which actions are sampled
     * based on the weighted approach as used in the Stoat paper.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public HeuristicalChromosomeFactory(int maxNumEvents) {
        this( true, maxNumEvents);
    }

    /**
     * Initialises a new chromosome factory that generates test cases which actions are sampled
     * based on the weighted approach as used in the Stoat paper. Uses pre-defined values for
     * the hyperparameters alpha, beta and gamma.
     *
     * @param resetApp Whether to reset the AUT before creating a new chromosome.
     * @param maxNumEvents The maximal number of actions per test case.
     */
    public HeuristicalChromosomeFactory(boolean resetApp, int maxNumEvents) {
        this(resetApp, maxNumEvents, 1, 0.3, 1.5);
    }

    /**
     * Initialises a new chromosome factory that generates test cases which actions are sampled
     * based on the weighted approach as used in the Stoat paper.
     *
     * @param resetApp Whether to reset the AUT before creating a new chromosome.
     * @param maxNumEvents The maximal number of actions per test case.
     * @param alpha The value for the hyperparameter alpha.
     * @param beta The value for the hyperparameter beta.
     * @param gamma The value for the hyperparameter gamma.
     */
    public HeuristicalChromosomeFactory(boolean resetApp, int maxNumEvents,
                                        double alpha, double beta, double gamma) {
        super(resetApp, maxNumEvents);
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    /**
     * Creates a new chromosome that generates a test case as described in the Stoat paper.
     *
     * @return Returns the generated chromosome.
     */
    @Override
    public IChromosome<TestCase> createChromosome() {
        IChromosome<TestCase> chromosome = super.createChromosome();

        // updates the unvisited widgets for the last executed action
        computeUnvisitedWidgets(uiAbstractionLayer.getExecutableUiActions());

        previousAction = null;

        return chromosome;
    }

    /**
     * Selects the next action to be included in the test case. Here the action with the highest
     * assigned weight is selected.
     *
     * @return Returns the selected action.
     */
    @Override
    protected Action selectAction() {

        List<UIAction> executableActions = uiAbstractionLayer.getExecutableUiActions();

        // compute unvisited widgets of previous action (if there is a previous action)
       computeUnvisitedWidgets(executableActions);

        // the actions with the highest weights represent candidates
        List<UIAction> candidateActions = new ArrayList<>();
        double maxWeight = 0.0;

        // derive the actions with the highest weights
        for (UIAction action : executableActions) {
            double weight = computeExecutionWeight(action);
            if( weight > maxWeight){
                candidateActions = new ArrayList<>();
                candidateActions.add(action);
                maxWeight = weight;
            } else if (weight == maxWeight) {
                candidateActions.add(action);
            }

            if (previousAction != null) {
                // add previously executed action to list of actions preceding an available widget
                String widgetId = action.getActivityName() + "->" + action.getActionType().name();
                if (action instanceof WidgetAction) {
                    widgetId = ((WidgetAction) action).getWidget().getId();
                }
                if (actionsPrecedingWidget.containsKey(widgetId)) {
                    actionsPrecedingWidget.get(widgetId).add(previousAction);
                } else {
                    actionsPrecedingWidget.put(widgetId, new HashSet<>(Collections.singletonList(previousAction)));
                }
            }
        }

        // select random action form candidates
        UIAction selectedAction = Randomness.randomElement(candidateActions);

        String widgetId = selectedAction.getActivityName() + "->" + selectedAction.getActionType().name();

        if (selectedAction instanceof WidgetAction) {
            widgetId = ((WidgetAction) selectedAction).getWidget().getId();
        }

        // update frequency
        if (executionCounter.containsKey(selectedAction)) {
            executionCounter.put(selectedAction, executionCounter.get(selectedAction) + 1);
        } else {
            executionCounter.put(selectedAction, 1);
        }

        if (previousAction != null) {
            // decrease the number of unvisited widgets, because this widget will be visited next
            if (!visitedWidgetIds.contains(widgetId)) {
                for (Action action : actionsPrecedingWidget.get(widgetId)) {
                    if (unvisitedChildWidgetCounter.get(action) > 0) {
                        unvisitedChildWidgetCounter.put(action, unvisitedChildWidgetCounter.get(action) - 1);
                    }
                }
                visitedWidgetIds.add(widgetId);
            }
        }

        previousAction = selectedAction;
        return selectedAction;
    }

    /**
     * Computes the weight for a given action.
     *
     * @param action The action for which the weight should be computed.
     * @return Returns the computed weight for the given action.
     */
    private double computeExecutionWeight(UIAction action) {

        // the weight depends on the action type
        double eventTypeWeight;
        switch (action.getActionType()) {
            case SWIPE_UP:
            case SWIPE_DOWN:
            case SWIPE_LEFT:
            case SWIPE_RIGHT:
            case BACK:
                eventTypeWeight = 0.5;
                break;
            case MENU:
                eventTypeWeight = 2;
                break;
            default:
                eventTypeWeight = 1;
                break;
        }

        int unvisitedChildren;
        if (unvisitedChildWidgetCounter.containsKey(action)) {
            unvisitedChildren = unvisitedChildWidgetCounter.get(action);
        } else {
            // twice highest if unknown
            int max = 0;
            for (Action key : unvisitedChildWidgetCounter.keySet()) {
                int current = unvisitedChildWidgetCounter.get(key);
                max = current > max ? current : max;
            }
            unvisitedChildren = max * 2;
        }

        // add 1 to not divide by zero
        int executionFrequency = (executionCounter.containsKey(action) ? executionCounter.get(action) : 0) + 1;

        return((alpha * eventTypeWeight) + (beta * unvisitedChildren)) / (gamma * executionFrequency);
    }

    /**
     * Computes the number of unvisited widgets caused through the last action.
     *
     * @param executableActions The list of available actions on the current screen.
     */
    private void computeUnvisitedWidgets(List<UIAction> executableActions) {
        if (previousAction != null) {
            int count = 0;
            for (UIAction action : executableActions) {

                String widgetId = action.getActivityName() + "->" + action.getActionType().name();

                if (action instanceof WidgetAction) {
                    widgetId = ((WidgetAction) action).getWidget().getId();
                }

                if (!visitedWidgetIds.contains(widgetId)) {
                   count++;
                }
            }
            unvisitedChildWidgetCounter.put(previousAction, count);
        }
    }
}
