package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.model.IGUIModel;

/**
 * Created by marceloeler on 11/07/17.
 */

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEAccManualExploration {


    @Test
    public void useAppContext() throws Exception {

        MATE.log("start testing acc");
        MATE mate = new MATE();
        //run mate for timeout minutes
        mate.testApp("AccManual");
    }
}
