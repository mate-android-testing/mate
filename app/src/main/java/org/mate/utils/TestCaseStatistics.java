package org.mate.utils;

import android.content.Intent;

import org.mate.MATE;
import org.mate.interaction.intent.IntentBasedAction;
import org.mate.interaction.intent.SystemAction;
import org.mate.interaction.ui.ActionType;
import org.mate.interaction.ui.Widget;
import org.mate.model.TestCase;
import org.mate.interaction.ui.Action;
import org.mate.interaction.ui.WidgetAction;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TestCaseStatistics {


    private TestCaseStatistics() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    public static void recordStats(TestCase testCase) {

        MATE.log("Visited Activities in Order:");
        for (int i = 0; i < testCase.getEventSequence().size(); i++) {
            MATE.log(testCase.getActivityBeforeAction(i));
        }

        countInvalidURIs(testCase);
        countComponentsPerType(testCase);
        countActionsPerType(testCase);
        countNullValues(testCase);
        printURIs(testCase);
        countWidgetActivity(testCase);
    }

    /**
     * Counts how often each widget has been triggered by a 'real' widget-related action,
     * e.g. a click, a long click or a text insertion. Also tracks the number of
     * widget-unrelated actions, e.g. a sleep or wake up action.
     */
    private static void countWidgetActivity(TestCase testCase) {

        // count how many actions per widgets have been executed
        Map<String, Integer> widgetActions = new HashMap<>();

        EnumSet<ActionType> widgetRelatedActions = EnumSet.of(ActionType.CLICK, ActionType.LONG_CLICK,
                ActionType.CLEAR_WIDGET, ActionType.TYPE_TEXT);

        // track the number of unrelated widget actions
        int widgetUnrelatedActions = 0;

        for (Action action : testCase.getEventSequence()) {
            if (action instanceof WidgetAction) {

                ActionType actionType = ((WidgetAction) action).getActionType();

                if (!widgetRelatedActions.contains(actionType)) {
                    widgetUnrelatedActions++;
                    continue;
                }

                Widget widget = ((WidgetAction) action).getWidget();

                String widgetName = widget.getClazz() + ":" + widget.getId();

                // with API level 24 (Java 8): Map.merge(key, 1, Integer::sum)
                if (widgetActions.containsKey(widgetName)) {
                    int currentCount = widgetActions.get(widgetName);
                    widgetActions.put(widgetName, currentCount+1);
                } else {
                    widgetActions.put(widgetName, 1);
                }
            }
        }

        // print how often each widget has been triggered
        for (Map.Entry<String, Integer> widgetEntry : widgetActions.entrySet()) {
            MATE.log_acc("Widget " + widgetEntry.getKey()
                    + " has been triggered: " + widgetEntry.getValue() + " times!");
        }

        // print the number of unrelated widget actions
        MATE.log_acc("Number of unrelated widget actions: " + widgetUnrelatedActions);
    }

    /**
     * Counts how often an activity, a service, a receiver, etc. is triggered by an intent
     * of the test case. We don't differentiate between a dynamic system event receiver and
     * a system event receiver; we count it as a system event receiver.
     *
     * @param testCase The test case to be inspected.
     */
    private static void countComponentsPerType(TestCase testCase) {

        List<Action> actions = testCase.getEventSequence();

        int activities = 0;
        int services = 0;
        int receivers = 0;
        int dynamicReceivers = 0;
        int systemEventReceivers = 0;

        for (Action action : actions) {

            if (action instanceof IntentBasedAction) {

                switch (((IntentBasedAction) action).getComponentType()) {

                    case ACTIVITY:
                        activities++;
                        break;
                    case SERVICE:
                        services++;
                        break;
                    case BROADCAST_RECEIVER:
                        Intent intent = ((IntentBasedAction) action).getIntent();

                        if (intent.getComponent() == null) {
                            // dynamic receivers can't receive an explicit intent
                            dynamicReceivers++;
                        } else {
                            receivers++;
                        }
                        break;
                }
            } else if (action instanceof SystemAction) {
                // we don't consider here dynamic system event receivers
                systemEventReceivers++;
            }
        }

        MATE.log("Number of targeted Activities: " + activities);
        MATE.log("Number of targeted Services: " + services);
        MATE.log("Number of targeted Receivers: " + receivers);
        MATE.log("Number of targeted dynamic Receivers: " + dynamicReceivers);
        MATE.log("Number of targeted system-event Receivers: " + systemEventReceivers);
    }

    private static void printURIs(TestCase testCase) {

        List<Action> actions = testCase.getEventSequence();

        for (Action action : actions) {

            if (action instanceof IntentBasedAction) {

                Intent intent = ((IntentBasedAction) action).getIntent();

                if (intent.getData() != null){
                    MATE.log("Generated URI: " + intent.getData());
                }
            }
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

    private static void countNullValues(TestCase testCase) {

        List<Action> actions = testCase.getEventSequence();

        int nullCtr = 0;

        for (Action action : actions) {
            if (action instanceof IntentBasedAction) {

                Intent intent = ((IntentBasedAction) action).getIntent();

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

        MATE.log("TestCase included null values: " + nullCtr);
    }

}

