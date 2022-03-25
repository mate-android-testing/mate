package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.rl.qlearning.autoblacktest.EpisodicExploration;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEAutoBlackTest {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting AutoBlackTest...");
        MATE mate = new MATE();

        EpisodicExploration episodicExploration = new EpisodicExploration();
        mate.testApp(episodicExploration);
    }
}
