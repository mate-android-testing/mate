package org.mate;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.heuristical.RandomExploration;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATERandomExplorationIntent {


    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Random Exploration...");

        MATE mate = new MATE();

        MATE.log_acc("Activities");
        for (String s : Registry.getEnvironmentManager().getActivityNames()) {
            MATE.log_acc("\t" + s);
        }

        MATE.log_acc("Relative Intent Amount: " + Properties.RELATIVE_INTENT_AMOUNT());

        final RandomExploration randomExploration
                = new RandomExploration(true, Properties.MAX_NUMBER_EVENTS(),
                Properties.RELATIVE_INTENT_AMOUNT());

        mate.testApp(randomExploration);
    }
}
