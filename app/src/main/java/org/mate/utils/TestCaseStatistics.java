package org.mate.utils;

import android.content.Intent;

import org.mate.MATE;

import org.mate.interaction.intent.ComponentDescription;
import org.mate.interaction.intent.IntentBasedAction;
import org.mate.interaction.intent.IntentFilterDescription;
import org.mate.interaction.intent.SystemAction;
import org.mate.model.TestCase;
import org.mate.ui.Action;
import org.mate.ui.WidgetAction;

import java.util.List;
import java.util.Set;

public final class TestCaseStatistics {


    private TestCaseStatistics() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    public static void recordStats(TestCase testCase, final List<ComponentDescription> components) {
        countInvalidURIs(testCase);
        countActionsPerType(testCase);

        if (components != null) {
            countNullValues(testCase, components);
        }
    }

    private static void countInvalidURIs(TestCase testCase) {

        List<Action> actions = testCase.getEventSequence();
        int countInvalidURIs = 0;
        int countTotalURIs = 0;

        for (Action action : actions) {

            if (action instanceof IntentBasedAction) {
                Intent intent = ((IntentBasedAction) action).getIntent();

                String uri = intent.getDataString();

                if (uri != null) {
                    MATE.log("URI: " + uri);
                    countTotalURIs++;
                    if (uri.equals("content:///") || uri.equals("file:///")) {
                        countInvalidURIs++;
                    }
                }
            }
        }

        MATE.log("Total Number of generated URIs: " + countTotalURIs);
        MATE.log("Number of invalid (empty) URIs: " + countInvalidURIs);
    }

    /**
     * Tracks the number of different actions, e.g. how many system actions vs ui actions.
     *
     * @param testCase The test case to be analysed.
     */
    private static void countActionsPerType(TestCase testCase) {

        List<Action> actions = testCase.getEventSequence();
        MATE.log("Total number of actions: " + actions);

        int numberOfUIActions = 0;
        int numberOfSystemActions = 0;
        int numberOfIntentBasedActions = 0;

        // track how many actions per type have been executed
        for (Action action : actions) {

            if (action instanceof WidgetAction) {
                numberOfUIActions++;
            } else if (action instanceof SystemAction) {
                numberOfSystemActions++;
            } else if (action instanceof IntentBasedAction) {
                numberOfIntentBasedActions++;
            }
        }

        MATE.log("Number of UI actions: " + numberOfUIActions);
        MATE.log("Number of intent-based actions: " + numberOfIntentBasedActions);
        MATE.log("Number of system actions: " + numberOfSystemActions);
    }

    private static void countNullValues(TestCase testCase, final List<ComponentDescription> components) {

        List<Action> actions = testCase.getEventSequence();

        int nullCtr = 0;

        for (Action action : actions) {
            if (action instanceof IntentBasedAction) {

                Intent intent = ((IntentBasedAction) action).getIntent();

                // get the corresponding component description
                String componentName = intent.getComponent().getClassName();
                ComponentDescription component =
                        ComponentDescription.getComponentByName(components, componentName);

                MATE.log("IntentBasedAction: " + action);
                MATE.log("Corresponding IntentFilter: " + component.getMatchingIntentFilter(intent));

                // actually each intent should have defined an action
                if (intent.getAction() == null) {
                    MATE.log("Found Intent without action: " + intent);
                    nullCtr++;
                }


            }
        }
    }

}

