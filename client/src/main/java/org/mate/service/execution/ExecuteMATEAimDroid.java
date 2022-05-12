package org.mate.service.execution;

import android.content.Context;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.rl.qlearning.aimdroid.ActivityInsulatedMultiLevelExploration;

public class ExecuteMATEAimDroid {

    public static void run(String packageName, Context context) {

        MATELog.log_acc("Starting AimDroid...");
        MATE mate = new MATE(packageName, context);

        ActivityInsulatedMultiLevelExploration activityInsulatedMultiLevelExploration
                = new ActivityInsulatedMultiLevelExploration(Properties.MIN_L(), Properties.MAX_L(),
                Properties.EPSILON(), Properties.ALPHA(), Properties.GAMMA());
        mate.testApp(activityInsulatedMultiLevelExploration);
    }

}
