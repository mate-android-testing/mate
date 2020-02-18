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

        MATE.log("Visited Activities in Order:");
        for (int i = 0; i < testCase.getEventSequence().size(); i++) {
            MATE.log(testCase.getActivityAfterAction(i));
        }

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
                    // MATE.log("URI: " + uri);
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
        MATE.log("Total number of actions: " + actions.size());

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
                ComponentDescription component = ((IntentBasedAction) action).getComponent();

                // get the corresponding intent filter description
                IntentFilterDescription intentFilter = ((IntentBasedAction) action).getIntentFilter();

                // MATE.log("" + action);

                // actually each intent should have defined an action
                if (intent.getAction() == null) {
                    // MATE.log("Found Intent without action!");
                    nullCtr++;
                }

                if (intent.getCategories() == null) {
                    // MATE.log("Found Intent without category!");
                    nullCtr++;
                }

                if (intent.getDataString() == null) {
                    // MATE.log("Found Intent without data URI!");
                    nullCtr++;
                }

                if (intent.getComponent() == null) {
                    // MATE.log("Found Intent without target component name!");
                    nullCtr++;
                }

                if (intent.getExtras() == null) {
                    // MATE.log("Found Intent without extras!");
                    nullCtr++;
                }
            }
        }

        MATE.log("TestCase included " + nullCtr + " null values!");
    }

}

