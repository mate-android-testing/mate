package org.mate.exploration.random;

import org.mate.MATE;
import org.mate.exceptions.AUTCrashException;
import org.mate.ui.Action;
import org.mate.interaction.DeviceMgr;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;

import java.util.Date;
import java.util.Random;
import java.util.Vector;

import static org.mate.MATE.device;

/**
 * Created by geyan on 11/06/2017.
 */

public class UniformRandom {
    private DeviceMgr deviceMgr;
    private String packageName;
    private Vector<Action> executableActions;

    public UniformRandom(DeviceMgr deviceMgr,
                         String packageName,MATE mate){
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
    }

    public void startUniformRandomExploration(IScreenState selectedScreenState, long runningTime) {

        long currentTime = new Date().getTime();
        int numberOfActions = 0;
        while (currentTime - runningTime <= MATE.TIME_OUT){
            System.out.println(currentTime - runningTime+" gap");
            //get a list of all executable actions as long as this state is different from last state
            executableActions = selectedScreenState.getActions();

            //select one action randomly
            Action action = executableActions.get(selectRandomAction(executableActions.size()));

            try {
                //execute this selected action
                deviceMgr.executeAction(action);
                numberOfActions++;

                String currentPackageName = device.getCurrentPackageName();

                //check the validity of current package after executing the selected action
                //check the random_lengh (number of actions before restarting the app)
                if (!currentPackageName.equals(this.packageName)||numberOfActions>=MATE.RANDOM_LENGH) {
                    deviceMgr.restartApp();
                    numberOfActions=0;
                }

                selectedScreenState = ScreenStateFactory.getScreenState("ActionsScreenState");
                //MATE.log(" time to get new state: " + (l2-l1));

            } catch (AUTCrashException e) {
                e.printStackTrace();
            }

            currentTime = new Date().getTime();
        }
    }

    public int selectRandomAction(int executionActionSize){
        Random rand = new Random();
        return rand.nextInt(executionActionSize);
    }
}
