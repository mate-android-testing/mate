package org.mate.service.execution;

import android.content.Context;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.rl.qlearning.autoblacktest.EpisodicExploration;

public class ExecuteMATEAutoBlackTest {

    public static void run(String packageName, Context context) {

        MATELog.log_acc("Starting AutoBlackTest...");
        MATE mate = new MATE(packageName, context);

        EpisodicExploration episodicExploration
                = new EpisodicExploration(Properties.ABT_MAX_NUM_OF_EPISODES(),
                Properties.ABT_MAX_EPISODE_LENGTH(), Properties.ABT_EPSILON(),
                Properties.ABT_DISCOUNT_FACTOR());
        mate.testApp(episodicExploration);
    }
}
