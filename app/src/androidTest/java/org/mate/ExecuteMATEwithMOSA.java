package org.mate;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.MOSA;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEwithMOSA {

    @Test
    public void useAppContext() throws Exception {

        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc(MOSA.ALGORITHM_NAME + " algorithm");

        MATE mate = new MATE();
        mate.testApp(MOSA.ALGORITHM_NAME);
    }
}
