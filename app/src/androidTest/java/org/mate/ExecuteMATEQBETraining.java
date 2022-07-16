package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.rl.qlearning.qbe.algorithms.QBETraining;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEQBETraining {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting QBE Training...");

        try (final MATE mate = new MATE()) {
            final QBETraining qbeTraining
                    = new QBETraining(Registry.getTimeout(), Properties.MAX_NUMBER_EVENTS());
            MATE.log_acc("Starting timeout run...");
            qbeTraining.run();
            MATE.log_acc("Finished run due to timeout.");
        }
    }
}
