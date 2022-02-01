package org.mate.service.execution;

import org.mate.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.heuristical.RandomExploration;

public class ExecuteMATERandomExplorationIntent {


    public static void run(String packageName, IRepresentationLayerInterface representationLayer) {
        MATE.log_acc("Starting Random Intent Exploration...");

        MATE mate = new MATE(packageName, representationLayer);

        MATE.log_acc("Relative Intent Amount: " + Properties.RELATIVE_INTENT_AMOUNT());

        final RandomExploration randomExploration
                = new RandomExploration(true, Properties.MAX_NUMBER_EVENTS(),
                Properties.RELATIVE_INTENT_AMOUNT());

        mate.testApp(randomExploration);
    }
}
