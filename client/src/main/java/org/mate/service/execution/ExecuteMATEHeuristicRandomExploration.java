package org.mate.service.execution;

import android.content.Context;

import org.mate.commons.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.heuristical.HeuristicExploration;

public class ExecuteMATEHeuristicRandomExploration {


    public static void run(String packageName, Context context) {

        MATELog.log_acc("Starting Heuristic Random Exploration...");

        MATE mate = new MATE(packageName, context);

        final HeuristicExploration heuristicExploration =
                new HeuristicExploration(Properties.MAX_NUMBER_EVENTS());

        mate.testApp(heuristicExploration);
    }
}
