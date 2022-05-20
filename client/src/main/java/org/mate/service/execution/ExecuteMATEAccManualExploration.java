package org.mate.service.execution;

import android.content.Context;

import org.mate.commons.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.manual.ManualExploration;

/**
 * Starts a manual exploration. The parameter 'enableAccessibilityChecks' controls
 * whether accessibility checks should be performed during exploration.
 */
public class ExecuteMATEAccManualExploration {

    public static void run(String packageName, Context context) {

        MATELog.log_acc("Starting manual exploration...");
        MATE mate = new MATE(packageName, context);

        ManualExploration manualExploration = new ManualExploration(false);
        mate.testApp(manualExploration);
    }
}
