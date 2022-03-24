package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.rl.qlearning.autodroid.EpisodicExploration;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEAutoDroid {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting AutoDroid...");
        MATE mate = new MATE();

        EpisodicExploration episodicExploration
                = new EpisodicExploration(Properties.MAX_NUM_OF_EPISODES(),
                Properties.MAX_EPISODE_LENGTH(), Properties.INITIAL_Q_VALUE());
        mate.testApp(episodicExploration);
    }
}
