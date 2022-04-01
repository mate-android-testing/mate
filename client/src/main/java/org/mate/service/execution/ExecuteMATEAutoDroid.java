package org.mate.service.execution;

import android.content.Context;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.rl.qlearning.autodroid.EpisodicExploration;

public class ExecuteMATEAutoDroid {

    public static void run(String packageName, Context context) {

        MATELog.log_acc("Starting AutoDroid...");
        MATE mate = new MATE(packageName, context);

        EpisodicExploration episodicExploration
                = new EpisodicExploration(Properties.MAX_NUM_OF_EPISODES(),
                Properties.MAX_EPISODE_LENGTH(), Properties.INITIAL_Q_VALUE(), Properties.P_HOME_BUTTON());
        mate.testApp(episodicExploration);
    }
}
