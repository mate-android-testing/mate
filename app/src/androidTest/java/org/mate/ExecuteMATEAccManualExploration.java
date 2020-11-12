package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.manual.ManualExploration;

import java.util.Date;

/**
 * Created by marceloeler on 11/07/17.
 */

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEAccManualExploration {

    private long runningTime;

    @Test
    public void useAppContext() {
        runningTime = new Date().getTime();

        MATE.log("start testing acc");
        MATE mate = new MATE();

        ManualExploration manualExploration = new ManualExploration();
        manualExploration.startManualExploration(runningTime);
    }
}
