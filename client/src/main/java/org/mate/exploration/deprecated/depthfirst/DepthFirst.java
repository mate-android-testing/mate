package org.mate.exploration.deprecated.depthfirst;

import static org.mate.commons.utils.MATELog.log;

import org.mate.Registry;
import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.exceptions.InvalidScreenStateException;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.ActionType;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.interaction.action.ui.WidgetAction;
import org.mate.commons.utils.MATELog;
import org.mate.interaction.DeviceMgr;
import org.mate.model.deprecated.graph.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.state.ScreenStateType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by geyan on 10/06/2017.
 */

@Deprecated
public class DepthFirst {
    private DeviceMgr deviceMgr;
    private String packageName;
    private IGUIModel guiModel;
    List<String> statesVisited;

    public DepthFirst(DeviceMgr deviceMgr, String packageName, IGUIModel guiModel){
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        this.guiModel = guiModel;
        statesVisited = new ArrayList<>();
    }

    public void startExploreDepthFirst(String selectedStateId, long t1){
        //mark the screen node/state as visited
        IScreenState selectedState = guiModel.getStateById(selectedStateId);
        statesVisited.add(selectedStateId);

        //gets a list of all executable actions
        //TODO: how about selectState.getactions
        List<WidgetAction> executableActions = selectedState.getWidgetActions();
        MATELog.log(selectedStateId + " - activity: " + selectedState.getActivityName());
        for (WidgetAction act: executableActions){
            Widget widget = act.getWidget();
            String widgetStr ="";
            if (widget!=null)
                widgetStr = widget.getId() + " - " + widget.getText() +" - " + widget.getClazz() + " - " + widget.getContentDesc();

            MATELog.log("..act: " + act.getActionType()+ " on "+ widgetStr);
        }

        List<Action> adjActions = new ArrayList<>();
        boolean deviceClosed=false;
        boolean stateFound=true;
        long t2 = new Date().getTime();
        boolean stopExecution=false;
        //for each possible executable action, execute, checks if a new state has been reached
        //if so, start the exploration from this new state
        for (int i=0; i<executableActions.size() && !stopExecution; i++){

            //gets an action to execute
            WidgetAction action = executableActions.get(i);

            //reaches the screen node/state being explored (backtrack)
            // stateFound = deviceMgr.goToState(guiModel,selectedStateId);
            stateFound = false;


            //if the app is in the screen node/state being explored
            if (stateFound) {
                MATELog.log("CURRENTLY IN STATE: " + selectedStateId);
                try {

                    if (action.getActionType()==ActionType.TYPE_TEXT) {
                        WidgetAction firstAction = action;
                        int contEditActions = 0;
                        boolean exit = false;
                        while (i<executableActions.size() && action.getActionType() == ActionType.TYPE_TEXT) {
                            contEditActions++;
                            MATELog.log((i + 1) + "/" + executableActions.size() + " txt: " + action.getWidget().getText());
                            this.deviceMgr.executeAction(action);
                            if (contEditActions>1){
                                MATELog.log("ADD Adj: " + action.getActionType() + " " + action.getWidget().getId());
                                firstAction.addAdjAction(action);
                            }
                            i++;
                            action=executableActions.get(i);
                        }
                        i--;
                        action = firstAction;
                    }
                    else{
                        MATELog.log((i + 1) + "/" + executableActions.size() + " txt: " + action.getWidget().getText());
                        this.deviceMgr.executeAction(action);
                    }


//                    action.setAdjActions(adjActions);

                    IScreenState state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

                    long timeToWait = waitForProgressBar(state);
                    state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

                    MATELog.log("\n\nnumber of actions (new state?): " + state.getActions().size()+"\n\n");

                    //if the same app is running, i.e., no other app is running
                    if (state.getPackageName().equals(this.packageName)) {
                        //creates a new node and or a new edge
                        guiModel.updateModel(action,state);
                        //if the action made the app to reach a different state
                        if (!selectedStateId.equals(guiModel.getCurrentStateId())) {
                            //when model is updated, the current screen changes
                            //if the reached screen node/state has not been explored yet
                            if (!statesVisited.contains(guiModel.getCurrentStateId())) {
                                startExploreDepthFirst(guiModel.getCurrentStateId(), t1);
                            }
                        }
                    }
                    else {
                        log("out of the application - different package");
                        log("package: " + state.getPackageName());
                        deviceMgr.restartApp();
                        state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
                        try {
                            guiModel.moveToState(state);
                        } catch (InvalidScreenStateException e) {
                            e.printStackTrace();
                            if (!statesVisited.contains(guiModel.getCurrentStateId()))
                                startExploreDepthFirst(guiModel.getCurrentStateId(),t1);
                        }
                    }
                } catch (AUTCrashException e) {
                    deviceMgr.pressHome();
                }
            }
            else{
                MATELog.log("STATE NOT FOUND");
            }

            t2 = new Date().getTime();
            deviceClosed = Registry.getDeviceMgr().getCurrentPackageName() == null;
            if (deviceClosed ||!stateFound || (t2-t1> Registry.getTimeout())) {
                stopExecution = true;
                MATELog.log("STOP execution");
            }
        }

        MATELog.log("EXIT STATE: "+selectedStateId);
    }

    private long waitForProgressBar(IScreenState state) {
        long ini = new Date().getTime();
        long end = new Date().getTime();
        boolean hasProgressBar = true;
        while (hasProgressBar && (end-ini)<22000) {
            hasProgressBar=false;
            for (Widget widget : state.getWidgets()) {
                if (widget.getClazz().contains("ProgressBar") && widget.isEnabled() && widget.getContentDesc().contains("Loading")) {
                    MATELog.log("WAITING PROGRESS BAR TO FINISH");
                    hasProgressBar = true;
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    state = ScreenStateFactory.getScreenState(state.getType());
                }
            }
            end = new Date().getTime();
        }
        return end-ini;
    }

}
