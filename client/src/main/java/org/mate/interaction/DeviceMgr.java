package org.mate.interaction;

import android.app.Instrumentation;
import android.os.Build;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import org.mate.Registry;
import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.intent.SystemAction;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Utils;
import org.mate.model.deprecated.graph.IGUIModel;
import org.mate.service.MATEService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The device manager is responsible for the actual execution of the various actions.
 * Also provides functionality to check for crashes, restart or re-install the AUT, etc.
 */
public class DeviceMgr {

    /**
     * The device instance provided by the instrumentation class to perform various actions.
     */
    private final UiDevice device;


    private final String packageName;


    public DeviceMgr(String packageName) {
        this.device = null;
        this.packageName = packageName;

    }

    /**
     * Executes a given action.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    public void executeAction(Action action) throws AUTCrashException {
        boolean success = false;
        try {
            success = MATEService.getRepresentationLayer().executeAction(action);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (!success && action instanceof SystemAction) {
            SystemAction systemAction = (SystemAction) action;
            // for System actions we can fall back to the MATE Server
            Registry.getEnvironmentManager().executeSystemEvent(Registry.getPackageName(),
                    systemAction.getReceiver(),
                    systemAction.getAction(),
                    systemAction.isDynamicReceiver());
        }

        checkForCrash();
    }

    /**
     * Checks whether a crash dialog appeared on the screen.
     *
     * @throws AUTCrashException Thrown when the last action caused a crash of the application.
     */
    private void checkForCrash() throws AUTCrashException {
        if (!MATEService.isRepresentationLayerAlive()) {
            MATELog.log("CRASH");
            throw new AUTCrashException("App crashed");
        }
    }

    /**
     * Checks whether the given widget represents a progress bar.
     *
     * @param widget The given widget.
     * @return Returns {@code true} if the widget refers to a progress bar,
     *         otherwise {@code false} is returned.
     */
    public boolean checkForProgressBar(Widget widget) {
        return widget.getClazz().contains("ProgressBar")
                && widget.isEnabled()
                && widget.getContentDesc().contains("Loading");
    }

    /**
     * Returns the screen width.
     *
     * @return Returns the screen width in pixels.
     */
    public int getScreenWidth() {
        try {
            return MATEService.getRepresentationLayer().getDisplayWidth();
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Returns the screen height.
     *
     * @return Returns the screen height in pixels.
     */
    public int getScreenHeight() {
        try {
            return MATEService.getRepresentationLayer().getDisplayHeight();
        } catch (RemoteException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Doesn't actually re-install the app, solely deletes the app's internal storage.
     */
    public void reinstallApp() {
        MATELog.log("Reinstall app");

        // Clears the files contained in the app-internal storage, i.e. the app is reset to its
        // original state.
        // This will also take care of re-generating an empty 'coverage.exec' file for those apps
        // that have been manually instrumented with Jacoco, otherwise the apps keep crashing.
        // I.e., it will execute "run-as <packageName> mkdir -p files" and "run-as <packageName>
        // touch files/coverage.exe"
        Registry.getEnvironmentManager().clearAppData();
    }

    /**
     * Restarts the AUT.
     */
    public void restartApp() {
        MATELog.log("Restarting app");
        MATEService.disconnectRepresentationLayer();
        Utils.sleep(200);
        MATEService.ensureRepresentationLayerIsConnected();
    }

    /**
     * Emulates pressing the 'HOME' button.
     */
    public void pressHome() {
        device.pressHome();
    }

    /**
     * Emulates pressing the 'BACK' button.
     */
    public void pressBack() {
        device.pressBack();
    }

    public String getCurrentPackageName() {
        try {
            return MATEService.getRepresentationLayer().getCurrentPackageName();
        } catch (RemoteException e) {
            if (e.getMessage() != null) {
                MATELog.log_warn(e.getMessage());
            }
            return null;
        }
    }

    /**
     * Retrieves the name of the currently visible activity.
     *
     * @return Returns the name of the currently visible activity.
     */
    public String getCurrentActivity() {
        try {
            String currentActivityName = MATEService.getRepresentationLayer().getCurrentActivityName();
            if (currentActivityName != null) {
                return currentActivityName;
            }
        } catch (RemoteException e) {
            if (e.getMessage() != null) {
                MATELog.log_warn(e.getMessage());
            }
        }

        // fall back mechanism (slow)
        return Registry.getEnvironmentManager().getCurrentActivityName();
    }

    /**
     * Grants the AUT the read and write runtime permissions for the external storage.
     * <p>
     * Depending on the API level, we can either use the very fast method grantRuntimePermissions()
     * (API >= 28) or the slow routine executeShellCommand(). Right now, however, we let the
     * MATE-Server granting those permissions directly before executing a privileged command in
     * order to avoid unnecessary requests.
     * <p>
     * In order to verify that the runtime permissions got granted, check the output of the
     * following command:
     * device.executeShellCommand("dumpsys package " + packageName);
     *
     * @return Returns {@code true} when operation succeeded, otherwise {@code false} is returned.
     */
    @SuppressWarnings("unused")
    public boolean grantRuntimePermissions() {

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        final String readPermission = "android.permission.READ_EXTERNAL_STORAGE";
        final String writePermission = "android.permission.WRITE_EXTERNAL_STORAGE";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            instrumentation.getUiAutomation().grantRuntimePermission(packageName, readPermission);
            instrumentation.getUiAutomation().grantRuntimePermission(packageName, writePermission);
            return true;
        }

        try {
            /*
             * The operation executeShellCommand() is costly, but unfortunately it is not possible
             * to concatenate two commands yet.
             */
            final String grantedReadPermission
                    = device.executeShellCommand("pm grant " + packageName + " " + readPermission);
            final String grantedWritePermission
                    = device.executeShellCommand("pm grant " + packageName + " " + writePermission);

            // an empty response indicates success of the operation
            return grantedReadPermission.isEmpty() && grantedWritePermission.isEmpty();
        } catch (IOException e) {
            MATELog.log_error("Couldn't grant runtime permissions!");
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the activity names of the AUT.
     *
     * @return Returns the activity names of the AUT.
     */
    public List<String> getActivityNames() {
        try {
            return MATEService.getRepresentationLayer().getTargetPackageActivityNames();
        } catch (RemoteException e) {
            MATELog.log_warn("Couldn't retrieve activity names!");
            if (e.getMessage() != null) {
                MATELog.log_warn(e.getMessage());
            }

            // fallback mechanism
            return Registry.getEnvironmentManager().getActivityNames();
        }
    }

    /**
     * Retrieves the stack trace of the last discovered crash.
     *
     * @return Returns the stack trace of the last crash.
     */
    public String getLastCrashStackTrace() {

        try {
            String response = device.executeShellCommand("run-as " + packageName
                    + " logcat -b crash -t 2000 AndroidRuntime:E *:S");

            List<String> lines = Arrays.asList(response.split("\n"));

            // traverse the stack trace from bottom up until we reach the beginning
            for (int i = lines.size() - 1; i >= 0; i--) {
                if (lines.get(i).contains("E AndroidRuntime: FATAL EXCEPTION: ")) {
                    return lines.subList(i, lines.size()).stream()
                            .collect(Collectors.joining("\n"));
                }
            }

        } catch(IOException e) {
            MATELog.log_warn("Couldn't retrieve stack trace of last crash!");
            if (e.getMessage() != null) {
                MATELog.log_warn(e.getMessage());
            }
        }

        // fallback mechanism
        return Registry.getEnvironmentManager().getLastCrashStackTrace();
    }

    @Deprecated
    public boolean goToState(IGUIModel guiModel, String targetScreenStateId) {
        return new GUIWalker(guiModel, packageName, this).goToState(targetScreenStateId);
    }

}
