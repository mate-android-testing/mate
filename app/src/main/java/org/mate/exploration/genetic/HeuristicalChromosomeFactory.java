package org.mate.exploration.genetic;

import org.mate.model.TestCase;
import org.mate.ui.Action;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.mate.ui.ActionType.BACK;
import static org.mate.ui.ActionType.MENU;
import static org.mate.ui.ActionType.SWIPE_DOWN;
import static org.mate.ui.ActionType.SWIPE_LEFT;
import static org.mate.ui.ActionType.SWIPE_RIGHT;
import static org.mate.ui.ActionType.SWIPE_UP;

public class HeuristicalChromosomeFactory extends AndroidRandomChromosomeFactory {

    private Map<Action, Integer> executionCounter = new HashMap<>();
    private Map<Action, Integer> unvisitedChildrenWidgetCounter = new HashMap<>();

    private double alpha, beta, gamma;

    private Action previousAction = null;

    public HeuristicalChromosomeFactory(int maxNumEvents) {
        super(maxNumEvents);
        this.alpha = 0.5;
        this.beta = 0.3;
        this.gamma = 0.4;
    }

    public HeuristicalChromosomeFactory(int maxNumEvents, double alpha, double beta, double gamma) {
        super(maxNumEvents);
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        IChromosome<TestCase> chromosome = super.createChromosome();

        //update unvisitedActions for last selected chromosome
        if (previousAction != null) {
            unvisitedChildrenWidgetCounter.put(previousAction, 0);
            for (Action action : uiAbstractionLayer.getExecutableActions()) {
                if (!executionCounter.containsKey(action)) {
                    unvisitedChildrenWidgetCounter.put(previousAction, unvisitedChildrenWidgetCounter.get(previousAction) + 1);
                }
            }
        }
        return chromosome;
    }

    @Override
    protected Action selectAction() {
        List<Action> executableActions = uiAbstractionLayer.getExecutableActions();

        //compute unvisited Actions of previous action (if there is a previous action)
        if (previousAction != null) {
            unvisitedChildrenWidgetCounter.put(previousAction, 0);
            for (Action action : executableActions) {
                if (!executionCounter.containsKey(action)) {
                    unvisitedChildrenWidgetCounter.put(previousAction, unvisitedChildrenWidgetCounter.get(previousAction) + 1);
                }
            }
        }

        //store all candidates with (same) highest weight in list
        List<Action> candidateActions = new ArrayList<>();
        double maxWeight = 0.0;

        for (Action action : executableActions) {
            double weight = computeExecutionWeight(action);
            if( weight > maxWeight){
                candidateActions = new ArrayList<>(Collections.singletonList(action));
                maxWeight = weight;
            } else if (weight == maxWeight) {
                candidateActions.add(action);
            }
        }

        //select random element form candidates
        Action selectedAction = Randomness.randomElement(candidateActions);

        //update frequency
        if (executionCounter.containsKey(selectedAction)) {
            executionCounter.put(selectedAction, executionCounter.get(selectedAction) + 1);
        } else {
            executionCounter.put(selectedAction, 1);
        }

        previousAction = selectedAction;

        return selectedAction;
    }

    private double computeExecutionWeight(Action action) {

        //compute weight for selected event type
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
        if (unvisitedChildrenWidgetCounter.containsKey(action)) {
            unvisitedChildren = unvisitedChildrenWidgetCounter.get(action);
        } else {
            //zero if value is unknown
            unvisitedChildren = 0;
        }

        //add 1 to not divide by zero
        int executionFrequency = (executionCounter.containsKey(action) ? executionCounter.get(action) : 0) + 1;

        double executionWeight = ((alpha * eventTypeWeight) + (beta * unvisitedChildren)) / (gamma * executionFrequency);

        return executionWeight;
    }
}
