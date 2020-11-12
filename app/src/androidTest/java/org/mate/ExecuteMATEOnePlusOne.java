package org.mate;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.deprecated.evolutionary.OnePlusOne;
import org.mate.model.TestCase;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEOnePlusOne {


    @Test
    public void useAppContext() {

        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("One-plus-one algorithm");

        MATE mate = new MATE();
       // mate.testApp("OnePlusOne");

        //Report
        List<TestCase> ts = new ArrayList<>(OnePlusOne.testsuite.values());
        MATE.log_acc("Final Report: test cases number = "+ts.size());

        //MATE.log_acc(OnePlusOne.coverageArchive.keySet().toString());
        //MATE.log_acc("Visited GUI States number = "+ OnePlusOne.coverageArchive.keySet().size());
        MATE.log_acc("Covered GUI States = "+ OnePlusOne.testsuite.get("0").getVisitedStates().size());


    }
}
