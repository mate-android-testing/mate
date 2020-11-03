package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEStandardGE {

    @Test
    public void useAppContext() throws Exception {
        MATE.log_acc("Starting Standard GE Algorithm...");

        MATE mate = new MATE();
        mate.testApp("StandardGE");
    }
}
