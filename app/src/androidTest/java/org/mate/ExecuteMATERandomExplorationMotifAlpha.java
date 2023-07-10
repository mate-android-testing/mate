package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.heuristical.RandomMotifExploration;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATERandomExplorationMotifAlpha {

    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Random Motif Alpha Exploration...");

        MATE mate = new MATE();

        MATE.log_acc("Alpha: " + Properties.MOTIF_ALPHA());

        final RandomMotifExploration randomExploration
                = new RandomMotifExploration(true, Properties.MAX_NUMBER_EVENTS());

        mate.testApp(randomExploration);
    }
}
