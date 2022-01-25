package org.mate.representation.commands;

import static org.mate.representation.commands.MessageHandler.GET_AVAILABLE_ACTIONS;

import android.os.RemoteException;

import org.mate.IMATEServiceInterface;
import org.mate.IRepresentationLayerInterface;
import org.mate.representation.util.MATERepLog;

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
    public void getAvailableActions() throws RemoteException {
        String threadName = Thread.currentThread().getName();
        MATERepLog.info("Command received: getAvailableActions() on thread " + threadName);
        messageHandler.handleMessage(messageHandler.obtainMessage(GET_AVAILABLE_ACTIONS, 0, 0));
        MATERepLog.info("Exiting Command: getAvailableActions()");
    }

    public void setMateService(IMATEServiceInterface mateService) {
        messageHandler.setMateService(mateService);
    }
}
