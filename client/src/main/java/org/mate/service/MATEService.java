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
import org.mate.commons.utils.MATELog;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Class in charge of firing up the MATE Client with the appropriate parameters.
 */
public class MATEService extends Service implements IBinder.DeathRecipient {

    public static final String PACKAGE_NAME_INTENT_EXTRA = "packageName";
    public static final String ALGORITHM_INTENT_EXTRA = "algorithm";

    /**
     * Representation Layer associated to the MATE Service.
     * To ensure that updates to this variable propagate predictable to other threads, we apply
     * the volatile modifier.
     */
    private volatile static IRepresentationLayerInterface representationLayer;
    private volatile static IBinder representationLayerBinder;

    /**
     * This CountDownLatch is used to wait in a non-main thread for the representation layer to be
     * connected, which happens on the main thread.
     */
    private static CountDownLatch representationLayerConnectionCountDown;

    public static void ensureRepresentationLayerIsConnected() {
        log(String.format("ensureRepresentationLayerIsConnected called on thread %s",
                Thread.currentThread().getName()));

        if (isRepresentationLayerAlive()) {
            // nothing to do
            return;
        }

        representationLayerConnectionCountDown = new CountDownLatch(1);

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

    private static void configureRepresentationLayer() {
        try {
            representationLayer.setTargetPackageName(Registry.getPackageName());
            // TODO (Ivan): This random seed should take into account how many actions we have
            //  executed so far. Otherwise, each time we reset exploration we start executing the
            //  same actions.
            representationLayer.setRandomSeed(Properties.RANDOM_SEED());
        } catch (RemoteException e) {
            throw new IllegalStateException("Couldn't configure Representation Layer");
        }
    }

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

    public static IRepresentationLayerInterface getRepresentationLayer() {
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
     * Notice that the "-e" flag indicate extra parameters that will be received in this method.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand called");

        if (intent == null) {
            log("MATE Service starting but intent was not provided");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!intent.hasExtra(ALGORITHM_INTENT_EXTRA)) {
            log("MATE Service starting but " + ALGORITHM_INTENT_EXTRA + " was not provided");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!intent.hasExtra(PACKAGE_NAME_INTENT_EXTRA)) {
            log("MATE Service starting but " + PACKAGE_NAME_INTENT_EXTRA + " was not provided");
            stopSelf();
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
        } catch (Exception e) {
            log("An exception occurred: " + e.getMessage());
            stopSelf();
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
        new Thread(() -> {
            try {
                MATERunner.run(packageName, algorithm, getApplicationContext());
            } catch (Exception e) {
                log("An exception occurred: " + e.getMessage());
                stopSelf();
            }
        }).start();

        // Do not restart this service if it is explicitly stopped, so return not sticky.
        return START_NOT_STICKY;
    }

    private boolean launchPackageName(String packageName) {
        Context context = getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);

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

        // TODO: process this information somehow
    }

    /**
     * Save representation layer interface stub and add listener in case it dies.
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

    private final IMATEServiceInterface.Stub binder = new IMATEServiceInterface.Stub() {
        @Override
        public void registerRepresentationLayer(IRepresentationLayerInterface representationLayer,
                                                IBinder binder) throws RemoteException {
            log("registerRepresentationLayer called");
            setRepresentationLayer(representationLayer, binder);
        }

        @Override
        public void reportAvailableActions(List<String> actions) throws RemoteException {
            log("reportAvailableActions called. Received actions: " + Arrays.toString(actions.toArray()));
        }
    };
}
