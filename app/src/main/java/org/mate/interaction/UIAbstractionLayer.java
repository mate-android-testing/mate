package org.mate.interaction;

import android.util.Log;

import org.mate.MATE;
import org.mate.exceptions.AUTCrashException;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.Widget;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static org.mate.MATE.device;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.FAILURE_APP_CRASH;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.FAILURE_EMULATOR_CRASH;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.FAILURE_UNKNOWN;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.SUCCESS;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.SUCCESS_NEW_STATE;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.SUCCESS_OUTBOUND;

public class UIAbstractionLayer {
    private String packageName;
    private DeviceMgr deviceMgr;
    private Map<Action, Edge> edges;
    private IScreenState currentScreenState;

    public UIAbstractionLayer(DeviceMgr deviceMgr, String packageName) {
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        edges = new HashMap<>();
        currentScreenState = ScreenStateFactory.getScreenState("ActionsScreenState");
    }

    public List<Action> getExecutableActions() {
        return getCurrentScreenState().getActions();
    }

    public ActionResult executeAction(Action action) {
        try {
            return executeActionUnsafe(action);
        } catch (Exception e) {
            Log.e("acc", "", e);
        }
        return FAILURE_UNKNOWN;
    }

    private ActionResult executeActionUnsafe(Action action) {
        IScreenState state;
        try {
            //execute this selected action
            deviceMgr.executeAction(action);

            //create an object that represents the screen
            //using type: ActionScreenState
        } catch (AUTCrashException e) {
            MATE.log_acc("CRASH MESSAGE" + e.getMessage());
            deviceMgr.handleCrashDialog();
            state = ScreenStateFactory.getScreenState("ActionsScreenState"); //TODO: maybe not needed
            edges.put(action, new Edge(action, currentScreenState, state));
            currentScreenState = state;

            return FAILURE_APP_CRASH;
        }

        state = ScreenStateFactory.getScreenState("ActionsScreenState");

        //check whether there is a progress bar on the screen
        long timeToWait = waitForProgressBar(state);
        //if there is a progress bar
        if (timeToWait > 0) {
            //add 2 sec just to be sure
            timeToWait += 2000;
            //set that the current action needs to wait before new action
            action.setTimeToWait(timeToWait);
            //get a new state
            state = ScreenStateFactory.getScreenState("ActionsScreenState");
        }

        //get the package name of the app currently running
        String currentPackageName = state.getPackageName();
        //check whether it is an installation package
        if (currentPackageName != null && currentPackageName.contains("com.google.android.packageinstaller")) {
            currentPackageName = handleAuth(deviceMgr, currentPackageName);
        }
        //if current package is null, emulator has crashed/closed
        if (currentPackageName == null) {
            MATE.log_acc("CURRENT PACKAGE: NULL");
            return FAILURE_EMULATOR_CRASH;
            //TODO: what to do when the emulator crashes?
        }

        //check whether the package of the app currently running is from the app under test
        //if it is not, restart app
        if (!currentPackageName.equals(this.packageName)) {
            MATE.log_acc("current package different from app package: " + currentPackageName);

            edges.put(action, new Edge(action, currentScreenState, state));
            currentScreenState = state;

            return SUCCESS_OUTBOUND;
        } else {
            //update model with new state
            edges.put(action, new Edge(action, currentScreenState, state));
            currentScreenState = state;

            /* Ignore this for now
            if (edges.put(action, state)) {
                MATE.log("New State found:" + state.getId());
                return SUCCESS_NEW_STATE;
            }*/

            return SUCCESS;
        }
    }

    public IScreenState getCurrentScreenState() {
        return currentScreenState;
    }

    private long waitForProgressBar(IScreenState state) {
        long ini = new Date().getTime();
        long end = new Date().getTime();
        boolean hadProgressBar = false;
        boolean hasProgressBar = true;
        while (hasProgressBar && (end - ini) < 22000) {
            hasProgressBar = false;
            for (Widget widget : state.getWidgets()) {
                if (widget.getClazz().contains("ProgressBar") && widget.isEnabled() && widget.getContentDesc().contains("Loading")) {
                    MATE.log("WAITING PROGRESS BAR TO FINISH");
                    hasProgressBar = true;
                    hadProgressBar = true;
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
        if (!hadProgressBar)
            return 0;
        return end - ini;
    }

    private String handleAuth(DeviceMgr dmgr, String currentPackage) {

        if (currentPackage.contains("com.google.android.packageinstaller")) {
            long timeA = new Date().getTime();


            boolean goOn = true;
            while (goOn) {

                IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
                Vector<Action> actions = screenState.getActions();
                for (Action action : actions) {
                    if (action.getWidget().getId().contains("allow")) {
                        try {
                            dmgr.executeAction(action);
                        } catch (AUTCrashException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }

                currentPackage = device.getCurrentPackageName();
                MATE.log("new package name: " + currentPackage);
                long timeB = new Date().getTime();
                if (timeB - timeA > 30000)
                    goOn = false;
                if (!currentPackage.contains("com.google.android.packageinstaller"))
                    goOn = false;
            }
            return currentPackage;
        } else
            return currentPackage;
    }

    public void resetApp() {
        deviceMgr.reinstallApp();
        sleep(5000);
        deviceMgr.restartApp();
        sleep(2000);
        currentScreenState = ScreenStateFactory.getScreenState("ActionsScreenState");
        String currentPackageName = currentScreenState.getPackageName();
        if (currentPackageName != null && currentPackageName.contains("com.google.android.packageinstaller")) {
            currentPackageName = handleAuth(deviceMgr, currentPackageName);
            currentScreenState = ScreenStateFactory.getScreenState("ActionsScreenState");
        }
    }

    public void restartApp() {
        deviceMgr.restartApp();
        sleep(2000);
        currentScreenState = ScreenStateFactory.getScreenState("ActionsScreenState");
        String currentPackageName = currentScreenState.getPackageName();
        if (currentPackageName != null && currentPackageName.contains("com.google.android.packageinstaller")) {
            currentPackageName = handleAuth(deviceMgr, currentPackageName);
            currentScreenState = ScreenStateFactory.getScreenState("ActionsScreenState");
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
        }
    }

    public Edge getEdge(Action action) {
        return edges.get(action);
    }

    public enum ActionResult {
        FAILURE_UNKNOWN, FAILURE_EMULATOR_CRASH, FAILURE_APP_CRASH, SUCCESS_NEW_STATE, SUCCESS, SUCCESS_OUTBOUND
    }

    public static class Edge {
        private Action action;
        private IScreenState source;
        private IScreenState target;

        public Edge(Action action, IScreenState source, IScreenState target) {
            this.action = action;
            this.source = source;
            this.target = target;
        }

        public Action getAction() {
            return action;
        }

        public IScreenState getSource() {
            return source;
        }

        public IScreenState getTarget() {
            return target;
        }
    }
}
