package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.manual.deprecated.ManualExploration;

/**
 * Starts a manual exploration. The parameter 'enableAccessibilityChecks' controls
 * whether accessibility checks should be performed during exploration.
 */
@RunWith(AndroidJUnit4.class)
public class ExecuteMATEAccManualExploration {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting manual exploration...");
        MATE mate = new MATE();

        ManualExploration manualExploration = new ManualExploration(false);
        mate.testApp(manualExploration);
    }
}
