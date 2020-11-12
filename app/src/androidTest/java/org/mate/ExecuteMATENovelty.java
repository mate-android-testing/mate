package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.deprecated.novelty.NoveltyBased;
import org.mate.model.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marceloeler on 11/07/17.
 */

@RunWith(AndroidJUnit4.class)
public class ExecuteMATENovelty {


    @Test
    public void useAppContext() {

        MATE.log_acc("Starting Novelty Search...");
        MATE mate = new MATE();
        //mate.testApp("Novelty");

        //Report
        List<TestCase> ts = new ArrayList<>(NoveltyBased.testsuite.values());
        MATE.log_acc("Final Report: test cases number = "+ts.size());

        MATE.log_acc(NoveltyBased.coverageArchive.keySet().toString());
        MATE.log_acc("Visited GUI States number = "+ NoveltyBased.coverageArchive.keySet().size());

        //visited activities
        //for(TestCase testcase : ts){

           // MATE.log_acc(testcase.getVisitedActivities().toString());
            //MATE.log_acc(testcase.getVisitedStates().toString());
        //}
        /**
         *



        String packageName = mate.getPackageName();
        IGUIModel guiModel = mate.getGuiModel();

        //needed to replay the test case
        String emulator = EnvironmentManager.detectEmulator(packageName);
        Instrumentation instrumentation = getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);
        DeviceMgr deviceMgr = new DeviceMgr(device, packageName);


        Vector<TestCase> ts = mate.getTestSuite();
        MATE.log_acc("NUM TEST CASES = "+ts.size());

        //visited activities
        for(TestCase testcase : ts){

            MATE.log_acc(testcase.getVisitedActivities().toString());
            MATE.log_acc(testcase.getVisitedStates().toString());
        }

        //Restart App
        MATE.log_acc("REPLAY");
        deviceMgr.reinstallApp();
        Thread.sleep(5000);
        deviceMgr.restartApp();
        Thread.sleep(2000);
        // /Replay first test case
        Vector<Action> tc = ts.get(0).getEventSequence();
        for (Action a : tc){
            MATE.log_acc("EVENTO X = "+a.getWidget()+" ; "+a.getActionType());
            deviceMgr.executeAction(a);
            Thread.sleep(1000);

        }
        MATE.log_acc("NUM EVENTS = "+tc.size());

        //produce summary - print to the log file
        //if (guiModel!=null) {
            //MATE.logsum("STATES_VISITED_BY_MATE:"+guiModel.getStates().size());
            //AccessibilitySummaryResults.printSummary(guiModel);
            //EnvironmentManager.deleteAllScreenShots(packageName);

        //}

         */


    }
}
