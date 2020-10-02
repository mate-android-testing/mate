package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.heuristical.RandomExploration;
import org.mate.utils.Coverage;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATERandomExploration {


    @Test
    public void useAppContext() throws Exception {

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

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE
                // TODO: handle combined activity coverage
                && Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {

            // store coverage of test case interrupted by timeout
            Registry.getEnvironmentManager().storeCoverageData(Properties.COVERAGE(),
                    "lastIncompleteTestCase", null);

            MATE.log_acc("Coverage of last test case: " +
                    Registry.getEnvironmentManager().getCoverage(Properties.COVERAGE()
                            ,"lastIncompleteTestCase"));

            // get combined coverage
            MATE.log_acc("Total coverage: "
                    + Registry.getEnvironmentManager()
                    .getCombinedCoverage(Properties.COVERAGE()));
        }
    }
}
