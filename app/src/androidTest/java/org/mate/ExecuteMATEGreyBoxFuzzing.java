package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.fuzzing.greybox.GreyBoxFuzzer;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEGreyBoxFuzzing {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting GreyBox Fuzzing...");

        MATE mate = new MATE();

        final GreyBoxFuzzer<?> greyBoxFuzzer = new GreyBoxFuzzer<>(
                // TODO: translate chromosome factory from property + other properties as well
                new AndroidRandomChromosomeFactory(true, Properties.MAX_NUMBER_EVENTS()),
                new CutPointMutationFunction(Properties.MAX_NUMBER_EVENTS()),
                new NeverTerminationCondition(),
                10
        );

        mate.testApp(greyBoxFuzzer);
        MATE.log_acc("Total number of crashes: " + greyBoxFuzzer.getCrashingInputs().size());
    }
}
