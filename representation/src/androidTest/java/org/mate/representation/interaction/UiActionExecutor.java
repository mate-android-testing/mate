package org.mate.representation.interaction;

import android.os.RemoteException;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.ActionType;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Utils;
import org.mate.representation.DeviceInfo;

import java.io.IOException;

/**
 * ActionExecutor class for UI actions.
 */
public class UiActionExecutor extends ActionExecutor {

    /**
     * The ADB command to to disable auto rotation.
     */
    private final String DISABLE_AUTO_ROTATION_CMD = "content insert " +
            "--uri content://settings/system --bind name:s:accelerometer_rotation --bind value:i:0";

    /**
     * The ADB command to rotate the emulator into portrait mode.
     */
    private final String PORTRAIT_MODE_CMD = "content insert --uri content://settings/system " +
            "--bind name:s:user_rotation --bind value:i:0";

    /**
     * The ADB command to rotate the emulator into landscape mode.
     */
    private final String LANDSCAPE_MODE_CMD = "content insert --uri content://settings/system " +
            "--bind name:s:user_rotation --bind value:i:1";

    /**
     * Executes a given action.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    @Override
    public boolean perform(Action action) throws AUTCrashException {
        return executeAction((UIAction) action);
    }

    /**
     * Executes the given ui action.
     *
     * @param action The given ui action.
     * @throws AUTCrashException If the app crashes.
     */
    private boolean executeAction(UIAction action) throws AUTCrashException {
        ActionType typeOfAction = action.getActionType();

        boolean success = false;

        switch (typeOfAction) {
            case BACK:
                success = device.pressBack();
                break;
            case MENU:
                success = device.pressMenu();
                break;
            case ENTER:
                success = device.pressEnter();
                break;
            case HOME:
                success = device.pressHome();
                break;
            case QUICK_SETTINGS:
                success = device.openQuickSettings();
                break;
            case SEARCH:
                success = device.pressSearch();
                break;
            case SLEEP:
                // Only reasonable when a wake up is performed soon, otherwise
                // succeeding actions have no effect.
                try {
                    device.sleep();
                    success = true;
                } catch (RemoteException e) {
                    MATELog.log("Sleep couldn't be performed");
                    e.printStackTrace();
                }
                break;
            case WAKE_UP:
                try {
                    device.wakeUp();
                    success = true;
                } catch (RemoteException e) {
                    MATELog.log("Wake up couldn't be performed");
                    e.printStackTrace();
                }
                break;
            case DELETE:
                success = device.pressDelete();
                break;
            case DPAD_UP:
                success = device.pressDPadUp();
                break;
            case DPAD_DOWN:
                success = device.pressDPadDown();
                break;
            case DPAD_LEFT:
                success = device.pressDPadLeft();
                break;
            case DPAD_RIGHT:
                success = device.pressDPadRight();
                break;
            case DPAD_CENTER:
                success = device.pressDPadCenter();
                break;
            case NOTIFICATIONS:
                success = device.openNotification();
                break;
            case TOGGLE_ROTATION:
                success = toggleRotation();
                break;
            case MANUAL_ACTION:
                // simulates a manual user interaction
                success = true;
                break;
            default:
                throw new UnsupportedOperationException("UI action "
                        + action.getActionType() + " not yet supported!");
        }

        return success;
    }

    /**
     * Toggles the rotation between portrait and landscape mode. Based on the following reference:
     * https://stackoverflow.com/questions/25864385/changing-android-device-orientation-with-adb
     */
    private boolean toggleRotation() {

        if (!DeviceInfo.getInstance().isDisabledAutoRotate()) {
            disableAutoRotation();
        }

        boolean success = false;

        try {
            String output = device.executeShellCommand(DeviceInfo.getInstance().isInPortraitMode() ?
                    LANDSCAPE_MODE_CMD :
                    PORTRAIT_MODE_CMD);
            if (!output.isEmpty()) {
                MATELog.log_warn("Couldn't toggle rotation: " + output);
            }

            DeviceInfo.getInstance().toggleInPortraitMode();

            success = true;
        } catch (IOException e) {
            MATELog.log_error("Couldn't change rotation!");
            throw new IllegalStateException(e);
        } finally {
            /*
             * After the rotation it takes some time that the device gets back in a stable state.
             * If we proceed too fast, the UIAutomator loses its connection. Thus, we insert a
             * minimal waiting time to avoid this problem.
             */
            Utils.sleep(100);
        }

        return success;
    }

    /**
     * Brings the emulator back into portrait mode.
     */
    public void setPortraitMode() {
        if (!DeviceInfo.getInstance().isDisabledAutoRotate()) {
            disableAutoRotation();
        }

        try {
            String output = device.executeShellCommand(PORTRAIT_MODE_CMD);
            if (!output.isEmpty()) {
                MATELog.log_warn("Couldn't change to portrait mode: " + output);
            }

            DeviceInfo.getInstance().setInPortraitMode(true);
        } catch (IOException e) {
            MATELog.log_error("Couldn't change to portrait mode!");
            throw new IllegalStateException(e);
        }
    }

    /**
     * Disables the auto rotation. Rotations don't have any effect if auto rotation is not disabled.
     */
    private void disableAutoRotation() {
        try {
            String output = device.executeShellCommand(DISABLE_AUTO_ROTATION_CMD);
            if (!output.isEmpty()) {
                MATELog.log_warn("Couldn't disable auto rotation: " + output);
            }

            DeviceInfo.getInstance().setDisabledAutoRotate(true);
        } catch (IOException e) {
            MATELog.log_error("Couldn't disable auto rotation!");
            throw new IllegalStateException(e);
        }
    }
}
