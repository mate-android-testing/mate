package org.mate.utils;

import android.content.Intent;

import org.mate.MATE;

import org.mate.interaction.intent.IntentBasedAction;
import org.mate.model.TestCase;
import org.mate.ui.Action;

import java.util.List;

public final class TestCaseStatistics {


    private TestCaseStatistics() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    public static void recordStats(TestCase testCase) {
        countInvalidURIs(testCase);
        countNullValues(testCase);
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

    private static void countNullValues(TestCase testCase) {

        List<Action> actions = testCase.getEventSequence();

        int nullCtr = 0;

        for (Action action : actions) {
            if (action instanceof IntentBasedAction) {
                Intent intent = ((IntentBasedAction) action).getIntent();

                if (intent.getAction() == null) {
                    nullCtr++;
                }


            }
        }
    }

}

