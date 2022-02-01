package org.mate.service.execution;

import org.mate.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.heuristical.HeuristicExploration;

public class ExecuteMATEHeuristicRandomExploration {


    public static void run(String packageName, IRepresentationLayerInterface representationLayer) {

        MATE.log_acc("Starting Heuristic Random Exploration...");

        MATE mate = new MATE(packageName, representationLayer);

        final HeuristicExploration heuristicExploration =
                new HeuristicExploration(Properties.MAX_NUMBER_EVENTS());

        mate.testApp(heuristicExploration);
    }
}
