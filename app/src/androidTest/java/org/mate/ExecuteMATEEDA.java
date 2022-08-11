package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.crash_reproduction.eda.EstimationOfDistribution;
import org.mate.crash_reproduction.fitness.CrashDistance;

import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEEDA {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting EDA strategy ...");
        MATE mate = new MATE();

        // Resetting app to make sure the root state is the same as when starting a testcase
        Registry.getUiAbstractionLayer().resetApp();

        EstimationOfDistribution estimationOfDistribution = new EstimationOfDistribution(
                Collections.singletonList(new CrashDistance()),
                Properties.DISTRIBUTION_MODEL().get(Properties.MODEL_REPRESENTATION().get(Properties.INITIALIZER().get())),
                Properties.POPULATION_SIZE());
        mate.testApp(estimationOfDistribution);
    }
}
