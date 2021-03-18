package org.mate;

import android.support.test.runner.AndroidJUnit4;

import android.support.test.uiautomator.UiDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.exploration.deprecated.random.UniformRandomForAccessibility;
import org.mate.interaction.DeviceMgr;
import org.mate.model.deprecated.graph.IGUIModel;
import org.mate.model.deprecated.graph.GraphGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.state.ScreenStateType;

import java.util.Date;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by marceloeler on 11/07/17.
 */

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEAccTestingRandom {

    private static IGUIModel guiModel;

    private static String packageName;

    private static UiDevice device;

    private long runningTime;

    private DeviceMgr deviceMgr;

    @Test
    public void useAppContext() {

        MATE.log("start testing acc");
        MATE mate = new MATE();

        device = UiDevice.getInstance(getInstrumentation());
        String emulator = Registry.getEnvironmentManager().allocateEmulator(this.packageName);
        runningTime = new Date().getTime();
        this.guiModel = new GraphGUIModel();

        if (emulator != null && !emulator.equals("")) {
            this.deviceMgr = new DeviceMgr(device, packageName);

            IScreenState initialScreenState = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
            //creates the graph that represents the GUI model
            this.guiModel = new GraphGUIModel();
            //first state (root node - action ==null)
            this.guiModel.updateModel(null, initialScreenState);
            UniformRandomForAccessibility unirandomacc = new UniformRandomForAccessibility(deviceMgr, packageName, guiModel, true);
            unirandomacc.startUniformRandomExploration(initialScreenState, runningTime);
        }

        //produce summary - print to the log file
        if (guiModel!=null) {
            //MATE.logsum("STATES_VISITED_BY_MATE:"+guiModel.getStates().size());
            AccessibilitySummaryResults.printSummary(guiModel);
            //EnvironmentManager.deleteAllScreenShots(packageName);

        }
    }
}