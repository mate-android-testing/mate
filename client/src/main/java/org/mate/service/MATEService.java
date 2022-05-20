package org.mate.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.R;
import org.mate.Registry;
import org.mate.commons.IMATEServiceInterface;
import org.mate.commons.IRepresentationLayerInterface;
import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Class in charge of firing up the MATE Client with the appropriate parameters.
 */
public class MATEService extends Service implements IBinder.DeathRecipient {

    /**
     * Name of intent extra indicating the package name on which to perform exploration.
     */
    public static final String PACKAGE_NAME_INTENT_EXTRA = "packageName";

    /**
     * Name of intent extra indicating the algorithm to be used for exploration.
     */
    public static final String ALGORITHM_INTENT_EXTRA = "algorithm";

    /**
     * Name of intent extra indicating that the MATE Service should wait that debugger is
     * attached before starting exploration.
     */
    public static final String WAIT_FOR_DEBUGGER = "wait-for-debugger";

    /**
     * The Representation Layer proxy object obtained after the representation layer registers
     * itself with our class.
     * To ensure that updates to this variable propagate predictable to other threads, we apply
     * the volatile modifier.
     */
    private volatile static IRepresentationLayerInterface representationLayer;

    /**
     * Representation Layer's Binder object.
     */
    private volatile static IBinder representationLayerBinder;

    /**
     * This CountDownLatch is used to wait in a non-main thread for the representation layer to be
     * connected, which happens on the main thread.
     */
    private static CountDownLatch representationLayerConnectionCountDown;

    /**
     * The number of times we have launched the representation layer.
     */
    private static int representationLayerLaunches = 0;

    /**
     * A boolean flag so that the MATE Service knows whether the MATE Client is running or not.
     */
    private volatile static boolean mateClientRunning = false;

    /**
     * A boolean flag that indicates that MATE Client/Service and Representation Layer should
     * wait for debugger to be attached to the Android Process.
     */
    private static boolean waitForDebugger = false;

    /**
     * Fires up Representation Layer if it is not already running.
     * Then, it waits for the Representation Layer to establish a connection with us and to
     * register itself afterwards.
     * If Representation Layer was already connected and alive, this method does nothing.
     */
    public static void ensureRepresentationLayerIsConnected() {
        log(String.format("ensureRepresentationLayerIsConnected called on thread %s",
                Thread.currentThread().getName()));

        if (isRepresentationLayerAlive()) {
            // nothing to do
            return;
        }

        representationLayerConnectionCountDown = new CountDownLatch(1);

        representationLayerLaunches++;

        boolean success = Registry.getEnvironmentManager().launchRepresentationLayer();
        if (!success) {
            throw new IllegalStateException("MATE Server was unable to launch representation layer");
        }

        try {
            representationLayerConnectionCountDown.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // do nothing
        }

        if (representationLayer == null) {
            throw new IllegalStateException("Unable to ensure that representation layer is connected");
        }

        // Config representation layer just connected
        configureRepresentationLayer();
    }

    /**
     * Configures Representation Layer after it has just started.
     * This mainly takes care of forwarding properties present in the MATE Client.
     * Moreover, it asks the Representation Layer to grant Storage runtime permissions for the AUT.
     * The latter is needed so that the Representation Layer can access files in the sdcard
     * (e.g., staticStrings.xml).
     */
    private static void configureRepresentationLayer() {
        try {
            if (waitForDebugger) {
                // The following does not seem to work.
                //  Android Studio does not show the process of the representation layer to
                //  attach to it. Maybe this is because the representation layer's process has
                //  the name of the AUT's package name.
                // representationLayer.waitForDebugger();
            }

            // Use the random seed, but also add an integer representing the number of times we
            // have launched the representation layer. This is to avoid using the same seed each
            // time we reset exploration, causing it to execute always the same actions.
            representationLayer.setRandomSeed(Properties.RANDOM_SEED() + representationLayerLaunches);

            representationLayer.setStateEquivalenceLevel(Properties.STATE_EQUIVALENCE_LEVEL());

            if (Registry.isReplayMode()) {
                representationLayer.setReplayMode();
            }

            if (Properties.WIDGET_BASED_ACTIONS()) {
                representationLayer.setWidgetBasedActions();
            }

            MATELog.log("Setting Storage runtime permissions for Representation Layer");
            representationLayer.grantRuntimePermission("android.permission.READ_EXTERNAL_STORAGE");
            representationLayer.grantRuntimePermission("android.permission.WRITE_EXTERNAL_STORAGE");

        } catch (RemoteException e) {
            throw new IllegalStateException("Couldn't configure Representation Layer");
        }
    }

    /**
     * Disconnects the currently connected Representation Layer.
     * Sadly, this can not be forced on a service client. We can only ask the representation
     * layer to gently exit itself from us.
     */
    public static void disconnectRepresentationLayer() {
        if (isRepresentationLayerAlive()) {
            try {
                representationLayer.exit();
            } catch (RemoteException e) {
                // do nothing
            }
        }

        representationLayer = null;
    }

    /**
     * @return whether we have a non-null proxy for the Representation Layer and it answers
     * successfully to the "ping" command.
     */
    public static boolean isRepresentationLayerAlive() {
        if (representationLayer == null) {
            return false;
        }

        try {
            representationLayer.ping();
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * @return the currently connected Representation Layer.
     * @throws AUTCrashException if object is null.
     */
    public static IRepresentationLayerInterface getRepresentationLayer() throws AUTCrashException {
        if (representationLayer == null) {
            throw new AUTCrashException("Trying to use a disconnected representation layer");
        }
        return representationLayer;
    }

    /**
     * Called when our service first comes to existence.
     * Here we make sure to launch a notification so we can then run as a foreground service.
     */
    @Override
    public void onCreate() {
        log("onCreate called");
        Notification notification = createNotification();
        startForeground(1337, notification);
    }

    /**
     * Called when another component requests tha the service be started.
     *
     * In our case, we will make such request using the following ADB command:
     * adb shell am start-foreground-service -n org.mate/.service.MATEService -e packageName
     * [aut-package-name] -e algorithm [mate-algorithm]
     *
     * Notice that the "-e" flag indicates extra parameters that will be received in this method.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand called");

        if (mateClientRunning) {
            log("MATE Service started, but MATE Client is already running. We'll do nothing");
            return START_NOT_STICKY;
        }

        if (intent == null) {
            log("MATE Service starting but intent was not provided");
            stopService();
            return START_NOT_STICKY;
        }

        log("MATE Service received the following intent extras:");
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                log(key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
            }
        }

        // Did user add "--ez wait-for-debugger true" to the startservice command?
        if (intent.hasExtra(WAIT_FOR_DEBUGGER) && intent.getBooleanExtra(WAIT_FOR_DEBUGGER, false)) {
            // attach the debugger via Run -> 'Attach Debugger to Android Process' -> 'org.mate'
            log("MATE Service waiting for Debugger to be attached to Android Process");
            waitForDebugger = true;
            Debug.waitForDebugger();
        }

        if (!intent.hasExtra(ALGORITHM_INTENT_EXTRA)) {
            log("MATE Service starting but " + ALGORITHM_INTENT_EXTRA + " was not provided");
            stopService();
            return START_NOT_STICKY;
        }

        if (!intent.hasExtra(PACKAGE_NAME_INTENT_EXTRA)) {
            log("MATE Service starting but " + PACKAGE_NAME_INTENT_EXTRA + " was not provided");
            stopService();
            return START_NOT_STICKY;
        }

        String algorithm = intent.getStringExtra(ALGORITHM_INTENT_EXTRA);
        String packageName = intent.getStringExtra(PACKAGE_NAME_INTENT_EXTRA);
        log(String.format("MATE Service starting for algorithm %s and package name %s",
                algorithm, packageName));

        try {
            if (!launchPackageName(packageName)) {
                log(String.format("MATE Service unable to start package name %s", packageName));
                return START_NOT_STICKY;
            }

            // Wait some time for AUT to start.
            // If we don't wait enough, we will fail allocation of emulator on the MATE Server
            // when it asks for the pid of the AUT and it doesn't find it.
            Utils.sleep(2000);

        } catch (Exception e) {
            log("An exception occurred trying to launch AUT: " + e.getMessage());
            stopService();
        }

        // Run MATE in a new thread.
        //
        // Since the MATE Service is the entry point of our program, it is run on the main thread
        // of this process.
        // If we try to run MATE Client directly on this process, we will later face a problem of
        // wanting to wait and execute at the same time on the same thread.
        // This happens because MATE.java will call MATEService.ensureRepresentationLayerIsConnected();
        // before spawning a new process, and so this code would be run on the main thread.
        // Once the Registry.getEnvironmentManager().launchRepresentationLayer(); method is
        // called, we would need to wait in the main thread for a onBind call. Nevertheless, the
        // onBind events are processed on the main thread. Therefore, by blocking the main thread
        // we are preventing the onBind event from happening.
        // Thus, we run MATE in a new thread.
        mateClientRunning = true;
        new Thread(() -> {
            try {
                MATERunner.run(packageName, algorithm, getApplicationContext());
            } catch (Exception e) {
                log("An exception occurred while running MATE Client: " + e.getMessage());

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                log(sw.toString());
            } finally {
                mateClientRunning = false;

                if (representationLayer != null) {
                    // ask the representation layer to disconnect itself from us, so we can stop
                    // the MATE Service gracefully.
                    log("MATE Service is asking representation layer to exit");
                    try {
                        representationLayer.exit();
                    } catch (RemoteException e) {
                        // do nothing
                    }
                }

                Utils.sleep(2000);

                MATEService.this.stopService();
            }
        }).start();

        // Do not restart this service if it is explicitly stopped, so return not sticky.
        return START_NOT_STICKY;
    }

    /**
     * Stop the MATE Service by any means necessary.
     */
    private void stopService() {
        if (representationLayer == null) {
            log("MATE Service finishing gracefully");
            stopForeground(true);
            stopSelf();
        } else {
            log("MATE Service finishing forcefully: representation layer won't disconnect");
            throw new IllegalStateException("MATE Service says bye bye");
        }
    }

    /**
     * Launches the Main activity of a package name.
     * This might fail if the request package does not have a Launchers specified in its
     * AndroidManifest.
     *
     * @param packageName
     * @return whether the activity was started successfully or not.
     */
    private boolean launchPackageName(String packageName) {
        Context context = getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);

        if (intent == null) {
            return false;
        }

        // Clear out any previous instances
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } catch (Exception e) {
            e.printStackTrace();
            MATELog.log("EXCEPTION CLEARING ACTIVITY FLAG");
            return false;
        }

        context.startActivity(intent);
        return true;
    }

    /**
     * Called when a client binds to this service.
     * E.g., when the Representation Layer binds to the MATE Service.
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        log("onBind called");
        log(String.format("onBind called on thread %s",
                Thread.currentThread().getName()));
        return binder;
    }

    /**
     * Called when the service is destroyed.
     */
    @Override
    public void onDestroy() {
        log("onDestroy called");
        super.onDestroy();
    }

    /**
     * Creates MATE Service's notification.
     * @return
     */
    private Notification createNotification() {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert(notificationManager != null);
            notificationManager.createNotificationChannelGroup(new NotificationChannelGroup("org_mate_group", "MATE Channel Group"));
            NotificationChannel notificationChannel = new NotificationChannel("org_mate_channel", "MATE Notifications Channel", NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(false);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "org_mate_channel");

        Intent notificationIntent = new Intent(this, MATE.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        builder.setContentTitle("MATE Service is running")
                .setTicker("MATE Service is running")
                .setContentText("Touch to open")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true);

        return builder.build();
    }

    public static void log(String message) {
        Log.i("MATE_SERVICE", String.format("[%d] %s", new Date().getTime(), message));
    }

    /**
     * Called when the Representation Layer's process is destroyed.
     */
    @Override
    public void binderDied() {
        log("Client just died");
        representationLayer = null;
        representationLayerBinder.unlinkToDeath(this,0);
    }

    /**
     * Save representation layer interface stub and add a listener in case it dies.
     *
     * @param representationLayer
     * @param binder
     */
    public void setRepresentationLayer(IRepresentationLayerInterface representationLayer, IBinder binder) {
        log(String.format("setRepresentationLayer called on thread %s",
                Thread.currentThread().getName()));

        MATEService.representationLayer = representationLayer;
        representationLayerBinder = binder;

        try {
            binder.linkToDeath(this, 0);
            log("Client death callback registered successfully");
        } catch (RemoteException e) {
            log("An error ocurred registering death listener " + e.getMessage());
        }

        representationLayerConnectionCountDown.countDown();
    }

    /**
     * MATE Service binder to return when a client connects with us.
     * Basically, it knows how to answer to commands that might be triggered by the service clients.
     */
    private final IMATEServiceInterface.Stub binder = new IMATEServiceInterface.Stub() {
        @Override
        public void registerRepresentationLayer(IRepresentationLayerInterface representationLayer,
                                                IBinder binder) throws RemoteException {
            log("registerRepresentationLayer called");
            setRepresentationLayer(representationLayer, binder);
        }
    };
}
