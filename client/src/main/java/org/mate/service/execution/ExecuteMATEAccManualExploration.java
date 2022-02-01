package org.mate.service.execution;

import org.mate.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.exploration.manual.ManualExploration;

/**
 * Starts a manual exploration. The parameter 'enableAccessibilityChecks' controls
 * whether accessibility checks should be performed during exploration.
 */
public class ExecuteMATEAccManualExploration {

    public static void run(String packageName, IRepresentationLayerInterface representationLayer) {

        MATE.log_acc("Starting manual exploration...");
        MATE mate = new MATE(packageName, representationLayer);

        ManualExploration manualExploration = new ManualExploration(false);
        mate.testApp(manualExploration);
    }
}
