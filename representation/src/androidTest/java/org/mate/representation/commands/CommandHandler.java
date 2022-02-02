package org.mate.representation.commands;

import static org.mate.representation.commands.MessageHandler.GET_AVAILABLE_ACTIONS;

import android.os.RemoteException;

import org.mate.commons.IMATEServiceInterface;
import org.mate.commons.IRepresentationLayerInterface;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.representation.DeviceInfo;
import org.mate.representation.state.widget.WidgetScreenParser;
import org.mate.representation.util.MATERepLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles commands requested by the MATE Service (e.g., fetch current available actions).
 *
 * Note that Inter Process Communication (IPC) calls are dispatched through a thread pool running
 * in each process, so the code executing here will NOT be running in our main thread like most
 * other things -- so, to update the UI (and use the Espresso API), we need to use a Handler to
 * hop over there.
 */
public class CommandHandler extends IRepresentationLayerInterface.Stub {

    private final MessageHandler messageHandler;

    public CommandHandler() {
        // since the Handler is created here, it will use the thread we are currently running on
        // (hopefully the Main thread).
        messageHandler = new MessageHandler();
    }

    @Override
    public void setTargetPackageName(String packageName) throws RemoteException {
        DeviceInfo.getInstance().setTargetPackageName(packageName);
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
    public String getCurrentPackageName() throws RemoteException {
        return DeviceInfo.getInstance().getCurrentPackageName();
    }

    @Override
    public String getCurrentActivityName() throws RemoteException {
        return DeviceInfo.getInstance().getCurrentActivityName();
    }

    @Override
    public List<String> getTargetPackageActivityNames() throws RemoteException {
        return DeviceInfo.getInstance().getTargetPackageActivityNames();
    }

    @Override
    public boolean isCrashDialogDisplayed() throws RemoteException {
        return DeviceInfo.getInstance().isCrashDialogDisplayed();
    }

    @Override
    public List<Widget> getCurrentScreenWidgets() throws RemoteException {
        return new WidgetScreenParser().getWidgets();
    }

    /*
    @Override
    public void getAvailableActions() throws RemoteException {
        String threadName = Thread.currentThread().getName();
        MATERepLog.info("Command received: getAvailableActions() on thread " + threadName);
        messageHandler.handleMessage(messageHandler.obtainMessage(GET_AVAILABLE_ACTIONS, 0, 0));
        MATERepLog.info("Exiting Command: getAvailableActions()");
    }
    */

    public void setMateService(IMATEServiceInterface mateService) {
        messageHandler.setMateService(mateService);
    }
}
