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

import org.mate.commons.IMATEServiceInterface;
import org.mate.commons.utils.MATELog;
import org.mate.representation.DynamicTest;
import org.mate.representation.commands.CommandHandler;
import org.mate.representation.util.MATERepLog;

/**
 * This class main's responsibility is to connect the Representation Layer with the MATE Service.
 *
 * In technical terms:
 * - First, this class establish an RPC connection through which the Representation Layer can
 * send commands to the MATE Service.
 * - Particularly, we use this initial connection to register ourselves with the MATE Service.
 * - After this, the MATE Service can send us commands using the provided interface.
 */
public class MATEServiceConnection implements ServiceConnection {
    /**
     * Package name of the MATE Client.
     */
    public static final String MATE_SERVICE_PACKAGE_NAME = "org.mate";

    /**
     * Fully qualified class name of the MATE Service.
     */
    public static final String MATE_SERVICE_CLASS_NAME = "org.mate.service.MATEService";

    /**
     * Current connection to the MATE Service.
     */
    public static MATEServiceConnection connection = null;

    /**
     * The MATE Service proxy object obtained after connection is established.
     */
    private IMATEServiceInterface mateService;

    /**
     * The command handler to resolve requests received from the MATE Service.
     */
    private final CommandHandler commandHandler;


    public MATEServiceConnection() {
        commandHandler = new CommandHandler();
    }

    /**
     * Init process of connection with the MATE Service.
     */
    public static void establish() throws Exception {
        // this is needed to create handlers in this thread
        Looper.prepare();

        String mainThreadName = Thread.currentThread().getName();
        MATERepLog.info("Dynamic Test started on thread " + mainThreadName);

        // create an instance of MATEServiceConnection, which will take care of listening for MATE
        // Service connection events.
        connection = new MATEServiceConnection();

        // bind to MATE Service using the connection object we just created.
        Intent intent = new Intent();
        intent.setClassName(MATE_SERVICE_PACKAGE_NAME, MATE_SERVICE_CLASS_NAME);

        final Context context = InstrumentationRegistry.getInstrumentation().getContext();
        boolean result = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);

        if (!result) {
            String message = "Unable to bind to MATE Service";
            MATERepLog.info(message);
            throw new Exception(message);
        }
    }

    /**
     * Tear down existing connection with the MATE Service.
     */
    public static void tearDownIfConnected() {
        if (connection == null) {
            return;
        }

        MATELog.log("MATE Representation Layer is tearing down connection with MATE Service.");

        final Context context = InstrumentationRegistry.getInstrumentation().getContext();
        context.unbindService(connection);
    }

    /**
     * Called when a connection to the MATE Service has been established, with the IBinder of the
     * communication channel to the Service.
     *
     * @param name
     * @param mateServiceBinder
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder mateServiceBinder) {
        MATERepLog.info("Representation layer connected to MATE Service");

        mateService = IMATEServiceInterface.Stub.asInterface(mateServiceBinder);
        commandHandler.setMateService(mateService);

        try {
            // we register with the MATE Service and pass a new Binder object so the service
            // knows if we go offline
            mateService.registerRepresentationLayer(commandHandler, new Binder());
        } catch (RemoteException e) {
            MATERepLog.info(e.toString());
        }
    }

    /**
     * Called when the connection to the MATE Service has been lost.
     * @param componentName
     */
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        MATERepLog.info("Representation layer disconnected from MATE Service");
        DynamicTest.keepRunning = false;
    }
}
