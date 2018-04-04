package org.mate;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.accessibility.AccessibilityInfoChecker;
import org.mate.accessibility.results.AccessibilitySummary;
import org.mate.datagen.DataGenerator;
import org.mate.datagen.Dictionary;
import org.mate.model.IGUIModel;
import org.mate.ui.EnvironmentManager;

import java.io.InputStream;

/**
 * Created by marceloeler on 11/07/17.
 */

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEAccTestingRandom {


    @Test
    public void useAppContext() throws Exception {

        MATE.log("start testing acc");
        MATE mate = new MATE();
        MATE.TIME_OUT = 30* 60 * 1000;
        long timeout = EnvironmentManager.getTimeout();
        MATE.TIME_OUT = timeout * 60 * 1000;
        MATE.log("TIMEOUT : " + timeout);


        MATE.TIME_OUT = timeout;
        MATE.RANDOM_LENGH = EnvironmentManager.getRandomLength();
        MATE.log("RANDOM length by server: " + MATE.RANDOM_LENGH);

        mate.testApp("AccRandom");
        String packageName = mate.getPackageName();
        IGUIModel guiModel = mate.getGuiModel();
        if (guiModel!=null) {
            //MATE.logsum("STATES_VISITED_BY_MATE:"+guiModel.getStates().size());
            AccessibilityInfoChecker.listFlawsByWidget(packageName,guiModel);
            AccessibilitySummary.printSummary(guiModel);
            EnvironmentManager.deleteAllScreenShots(packageName);
            //EnvironmentManager.logFlaws(packageName,mate.logMessage);
            EnvironmentManager.releaseEmulator();
        }
    }
}
