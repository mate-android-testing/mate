package org.mate.representation.mateservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import androidx.test.platform.app.InstrumentationRegistry;

import org.mate.IMATEServiceInterface;
import org.mate.representation.commands.CommandHandler;
import org.mate.representation.util.MATERepLog;

/**
 * This class main responsibility is to connect the Representation Layer with the MATE Service.
 *
 * In technical terms:
 * - First, this class establish an RPC connection through which the Representation Layer can
 * send commands to the MATE Service.
 * - Particularly, we use this initial connection to register ourselves with the MATE Service.
 * - After this, the MATE Service can send us commands using the provided interface.
 */
public class MATEServiceConnection implements ServiceConnection {
    public static final String MATE_SERVICE_PACKAGE_NAME = "org.mate";
    public static final String MATE_SERVICE_CLASS_NAME = "org.mate.MATEService";

    private final CommandHandler commandHandler;
    private IMATEServiceInterface mateService;

    public MATEServiceConnection() {
        commandHandler = new CommandHandler();
    }

    /**
     * Init process of connection with the MATE Service.
     */
    public static void establish() {
        // this is needed to create handlers in this thread
        Looper.prepare();

        String mainThreadName = Thread.currentThread().getName();
        MATERepLog.info("Dynamic Test started on thread " + mainThreadName);

        // create an instance of MATEServiceConnection, which will take care of listening for MATE
        // Service connection events.
        MATEServiceConnection connection = new MATEServiceConnection();

        // bind to MATE Service using the connection object we just created.
        Intent intent = new Intent();
        intent.setClassName(MATE_SERVICE_PACKAGE_NAME, MATE_SERVICE_CLASS_NAME);

        final Context context = InstrumentationRegistry.getInstrumentation().getContext();
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MATERepLog.info("Representation layer connected to MATE Service");

        mateService = IMATEServiceInterface.Stub.asInterface(service);
        commandHandler.setMateService(mateService);

        try {
            // we register with the MATE Service and pass a new Binder object so the service
            // knows if we go offline
            mateService.registerRepresentationLayer(commandHandler, new Binder());
        } catch (RemoteException e) {
            MATERepLog.info(e.toString());
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        MATERepLog.info("Representation layer disconnected from MATE Service");
    }
}
