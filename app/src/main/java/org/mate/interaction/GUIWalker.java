package org.mate.interaction;

import android.app.Instrumentation;

import org.mate.MATE;
import org.mate.exceptions.AUTCrashException;
import org.mate.exceptions.InvalidScreenStateException;
import org.mate.model.deprecated.graph.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.interaction.ui.WidgetAction;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by marceloeler on 21/06/17.
 */
@Deprecated
public class GUIWalker {

    private IGUIModel guiModelMgr;
    private String packageName;
    DeviceMgr deviceMgr;

    public GUIWalker(IGUIModel guiModel, String packageName, DeviceMgr deviceMgr){
        this.guiModelMgr = guiModel;
        this.packageName = packageName;
        Instrumentation instrumentation =  getInstrumentation();
        this.deviceMgr = deviceMgr;
    }

    private Action lastActionExecuted=null;



    private boolean checkStateReached(String targetScreenStateId) {
        IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");
        try {
            guiModelMgr.moveToState(state);
        } catch (InvalidScreenStateException e) {
            e.printStackTrace();
            return false;
        }
        if (guiModelMgr.getCurrentStateId().equals(targetScreenStateId))
            return true;
        else
            return false;
    }

    public boolean goToState(String targetScreenStateId) {
        boolean stateFound=false;

        if (guiModelMgr.getCurrentStateId().equals(targetScreenStateId)) {
            return true;
        }

        MATE.log("Going from " + guiModelMgr.getCurrentStateId() +" to " + targetScreenStateId);
//        MATE.log("         >>> 1st attempt");
        List<List<Action>> paths = guiModelMgr.pathFromTo(guiModelMgr.getCurrentStateId(),targetScreenStateId);
        if (paths.size()>0){
            executePaths(paths, targetScreenStateId);
            stateFound = this.checkStateReached(targetScreenStateId);
            if (stateFound) {
                return true;
            }
        }

//        MATE.log("         >>> 2nd attempt - restart");
        deviceMgr.restartApp();
        //MATE.log("State detected after restart: " + guiModelMgr.detectCurrentState(packageName,ScreenStateFactory.getScreenState("ActionsScreenState")));
        stateFound = this.checkStateReached(targetScreenStateId);
        if (stateFound) {
            return true;
        }
        paths = guiModelMgr.pathFromTo(guiModelMgr.getCurrentStateId(),targetScreenStateId);
        if (paths.size()>0){
            executePaths(paths,targetScreenStateId);
            stateFound = this.checkStateReached(targetScreenStateId);
            if (stateFound) {
                return true;
            }
        }

//        MATE.log("         >>> 3rd attempt - reinstall");
        deviceMgr.reinstallApp();
        deviceMgr.restartApp();
        //MATE.log("State detected after restart: " + guiModelMgr.detectCurrentState(packageName,ScreenStateFactory.getScreenState("ActionsScreenState")));
        stateFound = this.checkStateReached(targetScreenStateId);
        if (stateFound) {
            return true;
        }
        paths = guiModelMgr.pathFromTo(guiModelMgr.getCurrentStateId(),targetScreenStateId);
        if (paths.size()>0){
            executePaths(paths,targetScreenStateId);
            stateFound = this.checkStateReached(targetScreenStateId);
            if (stateFound) {
                return true;
            }
            deviceMgr.restartApp();
            stateFound = this.checkStateReached(targetScreenStateId);
            if (stateFound) {
                return true;
            }
        }
        return stateFound;
    }


    private void executePaths(List<List<Action>> paths, String targetScreenStateId) {
        boolean desiredStateReached = false;
        for (int ev = 0; ev < paths.size() && !desiredStateReached; ev++) {
            List<Action> path = paths.get(ev);
            goToState(guiModelMgr.getCurrentStateId());
            executePath(path, targetScreenStateId);
            desiredStateReached = checkStateReached(targetScreenStateId);
        }
    }

    private void executePath(List<Action> path, String targetScreenStateId){

        boolean desiredStateReached=false;
        List<Action> actions = new ArrayList<>();
        if (!path.isEmpty()){
            boolean targetReached = false;
            for (int i=0; i< path.size() && !targetReached; i++){

                Action action = path.get(i);

                actions.add(action);

                try {
                    deviceMgr.executeAction(action);
                    targetReached = this.checkStateReached(targetScreenStateId);
                    lastActionExecuted=action;

                    if (action instanceof WidgetAction) {
                        WidgetAction wa = (WidgetAction) action;
                        if (wa.getWidget().isEditable()) {
                            for (WidgetAction act : wa.getAdjActions())
                                deviceMgr.executeAction(act);
                        }
                    }

                } catch (AUTCrashException e) {
                    deviceMgr.pressHome();
                    i = path.size()+1; //exit loop
                }
            }
        }
    }
}
