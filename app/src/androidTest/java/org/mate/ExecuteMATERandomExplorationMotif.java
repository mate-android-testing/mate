package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.heuristical.RandomMotifExploration;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATERandomExplorationMotif {

    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Random Motif Exploration...");

        MATE mate = new MATE();

        MATE.log_acc("Relative Motif Action Amount: " + Properties.RELATIVE_MOTIF_ACTION_AMOUNT());

        final RandomMotifExploration randomExploration
                = new RandomMotifExploration(true, Properties.MAX_NUMBER_EVENTS(),
                Properties.RELATIVE_MOTIF_ACTION_AMOUNT());

        mate.testApp(randomExploration);
    }
}
