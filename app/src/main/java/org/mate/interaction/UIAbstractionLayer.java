package org.mate.interaction;

import android.os.RemoteException;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exceptions.AUTCrashException;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.interaction.ui.Action;
import org.mate.interaction.ui.PrimitiveAction;
import org.mate.interaction.ui.Widget;
import org.mate.interaction.ui.WidgetAction;
import org.mate.utils.Utils;

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
    private static final int UiAutomatorDisconnectedRetries = 3;
    private static final String UiAutomatorDisconnectedMessage = "UiAutomation not connected!";
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
        lastScreenState.setId("S0");
        screenStateEnumeration = 1;
    }

    /**
     * Returns the list of executable widget actions on the current screen.
     *
     * @return Returns the list of executable widget actions.
     */
    public List<WidgetAction> getExecutableActions() {
        return getLastScreenState().getActions();
    }

    /**
     * Executes the given action. Retries execution the action when the
     * UIAutomator is disconnected for a pre-defined number of times.
     *
     * @param action The action that should be executed.
     * @return Returns the outcome of the execution, e.g. success.
     */
    public ActionResult executeAction(Action action) {
        boolean retry = true;
        int retryCount = 0;

        /*
        * FIXME: The UIAutomator bug seems to be unresolvable right now.
        *  We have tried to restart the ADB server, but afterwards the
        *   connection is still broken.
         */
        while (retry) {
            retry = false;
            try {
                return executeActionUnsafe(action);
            } catch (Exception e) {
                if (e instanceof IllegalStateException
                        && e.getMessage().equals(UiAutomatorDisconnectedMessage)
                        && retryCount < UiAutomatorDisconnectedRetries) {
                    retry = true;
                    retryCount += 1;
                    continue;
                }
                Log.e("acc", "", e);
            }
        }
        return FAILURE_UNKNOWN;
    }

    /**
     * Executes the given action. As a side effect, the screen state
     * model is updated.
     *
     * @param action The action to be executed.
     * @return Returns the outcome of the execution, e.g. success.
     */
    private ActionResult executeActionUnsafe(Action action) {
        IScreenState state;
        try {
            deviceMgr.executeAction(action);
        } catch (AUTCrashException e) {
            MATE.log_acc("CRASH MESSAGE" + e.getMessage());
            deviceMgr.handleCrashDialog();
            if (action instanceof PrimitiveAction) {
                return FAILURE_APP_CRASH;
            }
            state = ScreenStateFactory.getScreenState("ActionsScreenState"); // TODO: maybe not needed
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

        // TODO: assess if timeout should be added to primitive actions as well
        // check whether there is a progress bar on the screen
        long timeToWait = waitForProgressBar(state);
        // if there is a progress bar
        if (timeToWait > 0) {
            // add 2 sec just to be sure
            timeToWait += 2000;
            // set that the current action needs to wait before new action
            if (action instanceof WidgetAction) {
                WidgetAction wa = (WidgetAction) action;
                wa.setTimeToWait(timeToWait);
            }
            clearScreen();
            // get a new state
            state = ScreenStateFactory.getScreenState("ActionsScreenState");
        }

        // get the package name of the app currently running
        String currentPackageName = state.getPackageName();

        // if current package is null, emulator has crashed/closed
        if (currentPackageName == null) {
            MATE.log_acc("CURRENT PACKAGE: NULL");
            return FAILURE_EMULATOR_CRASH;
            // TODO: what to do when the emulator crashes?
        }

        // check whether the package of the app currently running is from the app under test
        // if it is not, restart app
        if (!currentPackageName.equals(this.packageName)) {
            MATE.log("current package different from app package: " + currentPackageName);

            state = toRecordedScreenState(state);
            edges.put(action, new Edge(action, lastScreenState, state));
            lastScreenState = state;

            return SUCCESS_OUTBOUND;
        } else {
            // update model with new state
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

    /**
     * Returns the last recorded screen state.
     *
     * @return Returns the last recorded screen state.
     */
    public IScreenState getLastScreenState() {
        return lastScreenState;
    }

    private void clearScreen() {
        clearScreen(deviceMgr);
    }

    /**
     * Clears the screen from all sorts of dialogs. In particular, whenever
     * a permission dialog pops up, the permission is granted. If a crash dialog
     * appears, we press 'HOME'. If a google-sign dialog appears, the 'BACK'
     * button is pressed to return to the AUT. Clicks on 'OK' when a build
     * warning pops up.
     */
    public static void clearScreen(DeviceMgr deviceMgr) {
        boolean change = true;
        boolean retry = true;
        int retryCount = 0;

        while (change || retry) {
            retry = false;
            change = false;
            try {

                // check for crash dialog
                UiObject crashDialog1 = device.findObject(new UiSelector().packageName("android").textContains("keeps stopping"));
                UiObject crashDialog2 = device.findObject(new UiSelector().packageName("android").textContains("has stopped"));

                if (crashDialog1.exists() || crashDialog2.exists()) {
                    // TODO: Click 'OK' on crash dialog window rather than 'HOME'?
                    // press 'HOME' button
                    deviceMgr.handleCrashDialog();
                    change = true;
                    continue;
                }

                // check for outdated build warnings
                IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
                for (Widget widget : screenState.getWidgets()) {
                    if (widget.getText().equals("This app was built for an older version of Android and may not work properly. Try checking for updates, or contact the developer.")) {
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
                    // press BACK to return to AUT
                    MATE.log("Google Sign Dialog detected! Returning.");
                    deviceMgr.pressBack();
                    change = true;
                    continue;
                }

                // check for permission dialog
                if (screenState.getPackageName().equals("com.google.android.packageinstaller")
                        || screenState.getPackageName().equals("com.android.packageinstaller")) {
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
            } catch (Exception e) {
                if (e instanceof IllegalStateException
                        && e.getMessage().equals(UiAutomatorDisconnectedMessage)
                        && retryCount < UiAutomatorDisconnectedRetries) {
                    retry = true;
                    retryCount += 1;
                    continue;
                }
                Log.e("acc", "", e);
            }
        }
    }

    /**
     * Checks whether a progress bar appeared on the screen. If this is the case,
     * waits a certain amount of time that the progress bar can reach completion.
     *
     * @param state The recording of the current screen.
     * @return Returns the amount of time it has waited for completion.
     */
    private long waitForProgressBar(IScreenState state) {

        long ini = new Date().getTime();
        long end = new Date().getTime();
        boolean hadProgressBar = false;
        boolean hasProgressBar = true;

        // wait a certain amount of time (22 seconds at max)
        while (hasProgressBar && (end - ini) < 22000) {
            hasProgressBar = false;
            // check whether a widget represents a progress bar
            for (Widget widget : state.getWidgets()) {
                if (deviceMgr.checkForProgressBar(widget)) {
                    MATE.log("WAITING PROGRESS BAR TO FINISH");
                    hasProgressBar = true;
                    hadProgressBar = true;
                    Utils.sleep(3000);
                    state = ScreenStateFactory.getScreenState(state.getType());
                }
            }
            end = new Date().getTime();
        }
        if (!hadProgressBar)
            return 0;
        return end - ini;
    }

    /**
     * Resets an app, i.e. clearing the app cache and restarting the app.
     */
    public void resetApp() {
        try {
            device.wakeUp();
        } catch (RemoteException e) {
            MATE.log("Wake up couldn't be performed");
            e.printStackTrace();
        }
        Registry.getEnvironmentManager().setPortraitMode();
        deviceMgr.reinstallApp();
        Utils.sleep(5000);
        deviceMgr.restartApp();
        Utils.sleep(2000);
        clearScreen();
        if (Properties.WIDGET_BASED_ACTIONS()) {
            lastScreenState = toRecordedScreenState(ScreenStateFactory.getScreenState("ActionsScreenState"));
        }
    }

    /**
     * Restarts the app without clearing the app cache.
     */
    public void restartApp() {
        deviceMgr.restartApp();
        Utils.sleep(2000);
        clearScreen();
        if (Properties.WIDGET_BASED_ACTIONS()) {
            lastScreenState = toRecordedScreenState(ScreenStateFactory.getScreenState("ActionsScreenState"));
        }
    }

    /**
     * Returns the edge (a pair of screen states) that is described by the given action.
     *
     * @param action The given action.
     * @return Returns the edge belonging to the action.
     */
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
        screenState.setId("S" + screenStateEnumeration);
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

    /**
     * Represents an edge that links the connection between two screen states by an action.
     * Or more simply, executing the action on the source screen state leads to the target
     * screen state.
     */
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
