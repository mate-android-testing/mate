package org.mate.interaction;

import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;
import android.widget.TextView;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exceptions.AUTCrashException;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.ActionType;
import org.mate.ui.PrimitiveAction;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mate.MATE.device;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.FAILURE_APP_CRASH;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.FAILURE_EMULATOR_CRASH;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.FAILURE_UNKNOWN;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.SUCCESS;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.SUCCESS_OUTBOUND;

public class UIAbstractionLayer {
    private String packageName;
    private DeviceMgr deviceMgr;
    private Map<Action, Edge> edges;
    private IScreenState lastScreenState;
    private int screenStateEnumeration;

    public UIAbstractionLayer(DeviceMgr deviceMgr, String packageName) {
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        edges = new HashMap<>();
        clearScreen();
        lastScreenState = ScreenStateFactory.getScreenState("ActionsScreenState");
        lastScreenState.setId("S" + 0);
        screenStateEnumeration = 1;
    }

    public List<WidgetAction> getExecutableActions() {
        return getLastScreenState().getActions();
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
            if (action instanceof PrimitiveAction) {
                return FAILURE_APP_CRASH;
            }
            state = ScreenStateFactory.getScreenState("ActionsScreenState"); //TODO: maybe not needed
            state = toRecordedScreenState(state);
            edges.put(action, new Edge(action, lastScreenState, state));
            lastScreenState = state;

            return FAILURE_APP_CRASH;
        }

        if (action instanceof PrimitiveAction) {
            return SUCCESS;
        }

        clearScreen();
        state = ScreenStateFactory.getScreenState("ActionsScreenState");

        //Todo: assess if timeout should be added to primitive actions as well
        //check whether there is a progress bar on the screen
        long timeToWait = waitForProgressBar(state);
        //if there is a progress bar
        if (timeToWait > 0) {
            //add 2 sec just to be sure
            timeToWait += 2000;
            //set that the current action needs to wait before new action
            if (action instanceof WidgetAction) {
                WidgetAction wa = (WidgetAction) action;
                wa.setTimeToWait(timeToWait);
            }
            clearScreen();
            //get a new state
            state = ScreenStateFactory.getScreenState("ActionsScreenState");
        }

        //get the package name of the app currently running
        String currentPackageName = state.getPackageName();

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

            state = toRecordedScreenState(state);
            edges.put(action, new Edge(action, lastScreenState, state));
            lastScreenState = state;

            return SUCCESS_OUTBOUND;
        } else {
            //update model with new state
            state = toRecordedScreenState(state);
            edges.put(action, new Edge(action, lastScreenState, state));
            lastScreenState = state;

            /* Ignore this for now
            if (edges.put(action, state)) {
                MATE.log("New State found:" + state.getId());
                return SUCCESS_NEW_STATE;
            }*/

            return SUCCESS;
        }
    }

    public IScreenState getLastScreenState() {
        return lastScreenState;
    }

    private void clearScreen() {
        clearScreen(deviceMgr);
    }

    /**
     * Accept all permissions and ignore build for old android version warning.
     */
    public static void clearScreen(DeviceMgr deviceMgr) {
        boolean change = true;

        while (change) {
            change = false;
            // check for crash messages
            UiObject window = new UiObject(new UiSelector().packageName("android")
                    .textContains("has stopped"));
            if (window.exists()) {
                deviceMgr.handleCrashDialog();
                change = true;
                continue;
            } else {
                window = new UiObject(new UiSelector().packageName("android")
                    .textContains("keeps stopping"));
                if (window.exists()) {
                    deviceMgr.handleCrashDialog();
                    change = true;
                    continue;
                }
            }

            // check for outdated build warnings
            IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
            for (Widget widget : screenState.getWidgets()) {
                if(widget.getText().equals("This app was built for an older version of Android and may not work properly. Try checking for updates, or contact the developer.")) {
                    for (WidgetAction action : screenState.getActions()) {
                        if (action.getWidget().getText().equals("OK")) {
                            try {
                                deviceMgr.executeAction(action);
                                break;
                            } catch (AUTCrashException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    change = true;
                }
            }
            if (change) {
                continue;
            }

            // check for google sign in dialog
            if (screenState.getPackageName().equals("com.google.android.gms")) {
                try {
                    // press BACK to return to AUT
                    deviceMgr.executeAction(new WidgetAction(ActionType.BACK));
                } catch (AUTCrashException e) {
                    e.printStackTrace();
                }
                change = true;
                continue;
            }

            // check for permission dialog
            if (screenState.getPackageName().equals("com.google.android.packageinstaller")) {
                List<WidgetAction> actions = screenState.getActions();
                for (WidgetAction action : actions) {
                    if (action.getWidget().getId().contains("allow")) {
                        try {
                            deviceMgr.executeAction(action);
                        } catch (AUTCrashException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                change = true;
                continue;
            }
        }
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

    public void resetApp() {
        deviceMgr.reinstallApp();
        sleep(5000);
        deviceMgr.restartApp();
        sleep(2000);
        clearScreen();
        if (Properties.WIDGET_BASED_ACTIONS) {
            lastScreenState = toRecordedScreenState(ScreenStateFactory.getScreenState("ActionsScreenState"));
        }
    }

    public void restartApp() {
        deviceMgr.restartApp();
        sleep(2000);
        clearScreen();
        if (Properties.WIDGET_BASED_ACTIONS) {
            lastScreenState = toRecordedScreenState(ScreenStateFactory.getScreenState("ActionsScreenState"));
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

    public List<IScreenState> getRecordedScreenStates() {
        List<IScreenState> screenStates = new ArrayList<>();
        for (Edge edge : edges.values()) {
            if (!screenStates.contains(edge.source)) {
                screenStates.add(edge.source);
            }
            if (!screenStates.contains(edge.target)) {
                screenStates.add(edge.target);
            }
        }
        return screenStates;
    }



    public IScreenState toRecordedScreenState(IScreenState screenState) {
        List<IScreenState> recordedScreenStates = getRecordedScreenStates();
        for (IScreenState recordedScreenState : recordedScreenStates) {
            if (recordedScreenState.equals(screenState)) {
                return recordedScreenState;
            }
        }
        screenState.setId("S"+screenStateEnumeration);
        screenStateEnumeration++;
        return screenState;
    }

    public boolean checkIfNewState(IScreenState screenState) {
        List<IScreenState> recordedScreenStates = getRecordedScreenStates();
        for (IScreenState recordedScreenState : recordedScreenStates) {
            if (recordedScreenState.equals(screenState)) {
                return false;
            }
        }
        return true;
    }

    public IScreenState getStateFromModel(IScreenState screenState) {
        List<IScreenState> recordedScreenStates = getRecordedScreenStates();
        for (IScreenState recordedScreenState : recordedScreenStates) {
            if (recordedScreenState.equals(screenState)) {
                return recordedScreenState;
            }
        }
        return null;
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
