package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.manual.AskUserExploration;

/**
 * Starts a manual exploration.
 */
@RunWith(AndroidJUnit4.class)
public class ExecuteMATEAskUserExploration {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting manual exploration...");
        MATE mate = new MATE();

        AskUserExploration manualExploration = new AskUserExploration();
        mate.testApp(manualExploration);
    }
}
