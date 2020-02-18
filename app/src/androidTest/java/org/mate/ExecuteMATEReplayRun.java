package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEReplayRun {

    @Test
    public void useAppContext() throws Exception {
        MATE.log_acc("Replaying run...");

        MATE mate = new MATE();
        mate.testApp("Replaying");
    }
}
