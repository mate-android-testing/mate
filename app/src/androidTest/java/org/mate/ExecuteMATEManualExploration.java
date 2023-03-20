package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.manual.ManualExploration;

/**
 * Starts a manual exploration where the user is asked to supply an action or a command via a dialog.
 * NOTE: You need to invoke MATE-Server not in headless mode, otherwise the dialog can't be drawn!
 */
@RunWith(AndroidJUnit4.class)
public class ExecuteMATEManualExploration {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting manual exploration...");
        MATE mate = new MATE();

        ManualExploration manualExploration = new ManualExploration();
        mate.testApp(manualExploration);
    }
}
