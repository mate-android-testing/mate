package org.mate.interaction;

import android.os.Environment;
import android.os.RemoteException;

import org.mate.Registry;
import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.intent.SystemAction;
import org.mate.commons.interaction.action.ui.ActionType;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Utils;
import org.mate.service.MATEService;
import org.mate.state.IScreenState;
import org.mate.utils.StackTrace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The device manager is responsible for the actual execution of the various actions.
 * Also provides functionality to check for crashes, restart or re-install the AUT, etc.
 */
public class DeviceMgr {

    private final String packageName;

    /**
     * Initialises the device manager.
     *
     * @param packageName The package name of the AUT.
     */
    public DeviceMgr(String packageName) {
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
            MATELog.log_warn("A Remote Exception occurred while executing action");
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
        if (!MATEService.isRepresentationLayerAlive() || isCrashDialogPresent()) {
            // Either Representation Layer is not even alive, and thus AUT has crashed, or a
            // separate component has crashed and there is a crash dialog on the screen.
            MATELog.log("CRASH");
            throw new AUTCrashException("App crashed");
        }
    }

    /**
     * Checks whether a crash dialog is visible on the current screen.
     *
     * @return {@code true} if a crash dialog is visible, otherwise {@code false} is returned.
     */
    public boolean isCrashDialogPresent() {
        try {
            return MATEService.getRepresentationLayer().isCrashDialogPresent();
        } catch (RemoteException | AUTCrashException e) {
            MATELog.log_error("Unable to check if crash dialog is present");
            return false;
        }
    }

    /**
     * Checks whether the given screen contains a progress bar.
     *
     * @param screenState The given screen state.
     * @return Returns {@code true} if the screen contains a progress bar, otherwise {@code false}
     *          is returned.
     */
    public boolean checkForProgressBar(IScreenState screenState) {

        for (Widget widget : screenState.getWidgets()) {
            if (widget.isProgressBarType() && widget.isEnabled() && widget.isVisible()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the screen width.
     *
     * @return Returns the screen width in pixels.
     */
    public int getScreenWidth() {
        try {
            return MATEService.getRepresentationLayer().getDisplayWidth();
        } catch (RemoteException | AUTCrashException e) {
            MATELog.log_error("Unable to retrieve display width from representation " +
                    "layer");
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
        } catch (RemoteException | AUTCrashException e) {
            MATELog.log_error("Unable to retrieve display height from representation " +
                    "layer");
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
        // Note that execution of the "pm clear" command only drops the runtime permissions of the
        // AUT, not the MATE Client, and thus there is not problem with the writing of traces to disk.
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
        try {
            MATEService.getRepresentationLayer().executeAction(new UIAction(ActionType.HOME,
                    ""));
        } catch (RemoteException | AUTCrashException e) {
            if (e.getMessage() != null) {
                MATELog.log_warn(e.getMessage());
            }

            // do nothing
        }
    }

    /**
     * Emulates pressing the 'BACK' button.
     */
    public void pressBack() {
        try {
            MATEService.getRepresentationLayer().executeAction(new UIAction(ActionType.BACK,
                    ""));
        } catch (RemoteException | AUTCrashException e) {
            if (e.getMessage() != null) {
                MATELog.log_warn(e.getMessage());
            }

            // do nothing
        }
    }

    public String getRepresentationLayerTargetPackageName() {
        try {
            return MATEService.getRepresentationLayer().getTargetPackageName();
        } catch (RemoteException | AUTCrashException e) {
            if (e.getMessage() != null) {
                MATELog.log_warn(e.getMessage());
            }

            return null;
        }
    }

    public String getCurrentPackageName() {
        try {
            return MATEService.getRepresentationLayer().getCurrentPackageName();
        } catch (RemoteException | AUTCrashException e) {
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
            String currentActivityName = MATEService.getRepresentationLayer()
                    .getCurrentActivityName();
            if (currentActivityName != null) {
                return convertClassName(currentActivityName);
            }
        } catch (RemoteException | AUTCrashException e) {
            if (e.getMessage() != null) {
                MATELog.log_warn(e.getMessage());
            }
        }

        // fall back mechanism (slow)
        return convertClassName(Registry.getEnvironmentManager().getCurrentActivityName());
    }

    /**
     * Converts a fully-qualified class name to solely it's class name, i.e. the possibly redundant
     * package name is stripped off.
     *
     * @param className The fully-qualified class name consisting of <package-name>/<class-name>.
     * @return Returns the simple class name.
     */
    private String convertClassName(String className) {

        if (!className.contains("/")) {
            // the class name is already in its desired form
            return className;
        }

        String[] tokens = className.split("/");
        String packageName = tokens[0];
        String componentName = tokens[1];

        // if the component resides in the application package, a dot is used instead of the package name
        if (componentName.startsWith(".")) {
            componentName = packageName + componentName;
        }

        return componentName;
    }

    /**
     * Grants the AUT the read and write runtime permissions for the external storage.
     * <p>
     * Depending on the API level, we can either use the very fast method grantRuntimePermissions()
     * (API >= 28) or the slow routine executeShellCommand().
     * <p>
     * In order to verify that the runtime permissions got granted, check the output of the
     * following command:
     * device.executeShellCommand("dumpsys package " + packageName);
     *
     * @return Returns {@code true} when operation succeeded, otherwise {@code false} is returned.
     */
    @SuppressWarnings("unused")
    public boolean grantRuntimePermissions() throws AUTCrashException {

        final String readPermission = "android.permission.READ_EXTERNAL_STORAGE";
        final String writePermission = "android.permission.WRITE_EXTERNAL_STORAGE";

        try {
            MATEService.getRepresentationLayer().grantRuntimePermission(readPermission);
            MATEService.getRepresentationLayer().grantRuntimePermission(writePermission);
            return true;
        } catch (RemoteException e) {
            if (e.getMessage() != null) {
                MATELog.log_warn(e.getMessage());
            }

            // fallback mechanism
            return Registry.getEnvironmentManager().grantRuntimePermissions();
        }
    }

    /**
     * Returns the activities of the AUT.
     *
     * @return Returns the activities of the AUT.
     */
    public List<String> getActivities() {
        try {
            return MATEService.getRepresentationLayer().getTargetPackageActivityNames();
        } catch (RemoteException | AUTCrashException e) {
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
    public StackTrace getLastCrashStackTrace() {
        // Do not use the Representation Layer here.
        // If we have detected a failing action that led to a crash, then both AUT and
        // Representation Layer are dead. Thus, any request sent to the Representation Layer will
        // fail.

        return new StackTrace(Registry.getEnvironmentManager().getLastCrashStackTrace());
    }

    public File getTargetPackageFilesDir() {
        try {
            return new File(MATEService.getRepresentationLayer().getTargetPackageFilesDir());
        } catch (RemoteException | AUTCrashException e) {
            MATELog.log_warn("Couldn't retrieve target package files dir!");
            if (e.getMessage() != null) {
                MATELog.log_warn(e.getMessage());
            }
        }

        // "fallback" mechanism
        return new File(String.format("/data/data/%s/files", Registry.getPackageName()));
    }

    /**
     * Sends a broadcast to the tracer, which in turn dumps the collected traces to a file on
     * the external storage.
     */
    private void sendBroadcastToTracer() {
        try {
            MATEService.getRepresentationLayer().sendBroadcastToTracer();
        } catch (Exception e) {
            // do nothing
        }
    }

    /**
     * Reads the traces from the external memory and deletes afterwards the traces file.
     *
     * @return Returns the set of traces.
     *
     */
    public Set<String> getTraces() {

        // triggers the dumping of traces to a file called traces.txt
        sendBroadcastToTracer();

        File sdCard = Environment.getExternalStorageDirectory();
        File infoFile = new File(sdCard, "info.txt");

        /*
         * We need to wait until the info.txt file is generated, once it is there, we know that all
         * traces have been dumped.
         */
        while(!infoFile.exists()) {
            MATELog.log("Waiting for info.txt...");
            Utils.sleep(200);
        }

        File traceFile = new File(sdCard, "traces.txt");

        if (!traceFile.exists()) {
            throw new IllegalStateException("The file traces.txt doesn't exist!");
        }

        Set<String> traces = new HashSet<>();

        try (BufferedReader reader
                     = new BufferedReader(new InputStreamReader(new FileInputStream(traceFile)))) {

            String line = reader.readLine();

            while (line != null) {
                traces.add(line);
                line = reader.readLine();
            }

        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read traces!", e);
        }

        // remove both files
        traceFile.delete();
        infoFile.delete();

        return traces;
    }

    /**
     * Stores the traces to a file called traces.txt on the external memory.
     *
     * @param traces The traces to be stored.
     */
    public void storeTraces(Set<String> traces) {

        File sdCard = Environment.getExternalStorageDirectory();
        File traceFile = new File(sdCard, "traces.txt");

        try (Writer fileWriter = new FileWriter(traceFile)) {

            for(String trace : traces) {
                fileWriter.write(trace);
                fileWriter.write(System.lineSeparator());
            }

            fileWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't write to traces.txt!", e);
        }

        /*
         * The info.txt indicates that the dumping of traces has been completed and it contains
         * the number of written traces.
         */
        File infoFile = new File(sdCard, "info.txt");

        try (Writer fileWriter = new FileWriter(infoFile)) {
            fileWriter.write(traces.size());
            fileWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't write to info.txt!", e);
        }
    }
}
