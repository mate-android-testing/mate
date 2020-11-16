package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.heuristical.RandomExploration;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATERandomExploration {


    @Test
    public void useAppContext() {

        MATE.log_acc("Starting Random Exploration...");

        MATE mate = new MATE();

        final RandomExploration randomExploration
                = new RandomExploration(true, Properties.MAX_NUMBER_EVENTS());

        mate.testApp(randomExploration);
    }
}
