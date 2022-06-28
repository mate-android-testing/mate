package org.mate.representation.commands;

import android.os.Debug;
import android.os.RemoteException;

import org.mate.commons.IMATEServiceInterface;
import org.mate.commons.IRepresentationLayerInterface;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.state.equivalence.StateEquivalenceLevel;
import org.mate.commons.utils.MATELog;
import org.mate.representation.DeviceInfo;
import org.mate.representation.DynamicTest;
import org.mate.representation.ExplorationInfo;
import org.mate.representation.interaction.ActionExecutor;
import org.mate.representation.interaction.ActionExecutorFactory;
import org.mate.representation.state.widget.WidgetScreenParser;
import org.mate.representation.test.BuildConfig;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Handles commands requested by the MATE Service (e.g., fetch current available actions).
 */
public class CommandHandler extends IRepresentationLayerInterface.Stub {

    public CommandHandler() {}

    @Override
    public void ping() throws RemoteException {
        // Do nothing. This method is used to test the connection between the MATE Service and
        // the Representation Layer.
    }

    @Override
    public void exit() throws RemoteException {
        MATELog.log("MATE Representation Layer was asked to exit.");
        DynamicTest.keepRunning = false;
    }

    @Override
    public void waitForDebugger() throws RemoteException {
        MATELog.log("MATE Representation Layer waiting for Debugger to be attached to Android " +
                "Process");
        Debug.waitForDebugger();
    }

    @Override
    public String getTargetPackageName() throws RemoteException {
        return BuildConfig.TARGET_PACKAGE_NAME;
    }

    @Override
    public void setRandomSeed(long seed) throws RemoteException {
        ExplorationInfo.getInstance().setRandomSeed(seed);
    }

    @Override
    public int getDisplayWidth() throws RemoteException {
        return DeviceInfo.getInstance().getDisplayWidth();
    }

    @Override
    public int getDisplayHeight() throws RemoteException {
        return DeviceInfo.getInstance().getDisplayHeight();
    }

    @Override
    public boolean grantRuntimePermission(String permission) throws RemoteException {
        return DeviceInfo.getInstance().grantRuntimePermission(permission);
    }

    @Override
    public boolean isCrashDialogPresent() throws RemoteException {
        return DeviceInfo.getInstance().isCrashDialogPresent();
    }

    @Override
    public String getTargetPackageFilesDir() throws RemoteException {
        return DeviceInfo.getInstance().getTargetPackageFilesDir();
    }

    @Override
    public void sendBroadcastToTracer() throws RemoteException {
        ExplorationInfo.getInstance().sendBroadcastToTracer();
    }

    @Override
    public String getCurrentPackageName() throws RemoteException {
        return ExplorationInfo.getInstance().getCurrentPackageName();
    }

    @Override
    public String getCurrentActivityName() throws RemoteException {
        return ExplorationInfo.getInstance().getCurrentActivityName();
    }

    @Override
    public List<String> getTargetPackageActivityNames() throws RemoteException {
        return ExplorationInfo.getInstance().getTargetPackageActivityNames();
    }

    @Override
    public String executeShellCommand(String command) throws RemoteException {
        return DeviceInfo.getInstance().executeShellCommand(command);
    }

    @Override
    public boolean executeAction(Action action) throws RemoteException {
        if (action == null) {
            MATELog.log_error("Trying to execute null action");
            throw new IllegalStateException("executeAction method on representation layer was " +
                    "called for a null action");
        }

        ActionExecutor executor = ActionExecutorFactory.getExecutor(action);

        try {
            executor.perform(action);
            return true;
        } catch (Exception e) {
            MATELog.log_error(
                    "An exception occurred executing action on representation layer: " +
                    e.getMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            MATELog.log_error(sw.toString());

            return false;
        }
    }

    @Override
    public List<Widget> getCurrentScreenWidgets() throws RemoteException {
        try {
            return new WidgetScreenParser().getWidgets();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();

            MATELog.log_error(String.format("Exception occurred: %s", stackTrace));

            throw e;
        }
    }

    @Override
    public void setReplayMode() throws RemoteException {
        ExplorationInfo.getInstance().setReplayMode();
    }

    @Override
    public void setWidgetBasedActions() throws RemoteException {
        ExplorationInfo.getInstance().setWidgetBasedActions();
    }

    @Override
    public void setStateEquivalenceLevel(StateEquivalenceLevel level) throws RemoteException {
        ExplorationInfo.getInstance().setStateEquivalenceLevel(level);
    }

    public void setMateService(IMATEServiceInterface mateService) {
        // do nothing, for now
    }
}
