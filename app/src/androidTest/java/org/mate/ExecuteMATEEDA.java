package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.crash_reproduction.eda.EstimationOfDistribution;
import org.mate.crash_reproduction.eda.univariate.StoatProbabilityInitialization;
import org.mate.crash_reproduction.fitness.CrashDistance;

import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEEDA {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting EDA strategy ...");
        MATE mate = new MATE();


        EstimationOfDistribution estimationOfDistribution = new EstimationOfDistribution(
                Collections.singletonList(new CrashDistance()),
                Properties.DISTRIBUTION_MODEL().get(Properties.MODEL_REPRESENTATION().get(new StoatProbabilityInitialization(0.6))),
                Properties.POPULATION_SIZE());
        mate.testApp(estimationOfDistribution);
    }
}
