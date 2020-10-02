package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.heuristical.HeuristicExploration;
import org.mate.utils.Coverage;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEHeuristicRandomExploration {


    @Test
    public void useAppContext() throws Exception {

        MATE.log_acc("Starting Heuristic Random Exploration...");

        MATE mate = new MATE();

        MATE.log_acc("Activities");
        for (String s : Registry.getEnvironmentManager().getActivityNames()) {
            MATE.log_acc("\t" + s);
        }

        final HeuristicExploration heuristicExploration =
                new HeuristicExploration(Properties.MAX_NUM_EVENTS());

        mate.testApp(heuristicExploration);

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE
                // TODO: handle combined activity coverage
                && Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {

            // store coverage of test case interrupted by timeout
            Registry.getEnvironmentManager().storeCoverageData(Properties.COVERAGE(),
                    "lastIncompleteTestCase", null);

            // get combined coverage
            MATE.log_acc("Total coverage: "
                    + Registry.getEnvironmentManager()
                    .getCombinedCoverage(Properties.COVERAGE()));
        }
    }
}
