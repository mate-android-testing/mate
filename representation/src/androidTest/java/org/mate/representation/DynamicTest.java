package org.mate.representation;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.representation.mateservice.MATEServiceConnection;

/**
 * DynamicTest is a special Espresso test.
 * The name comes from the fact that it does not have a fixed sequence of events to perform in
 * its code. On the other hand, it only launches the Main activity of the AUT and then waits for
 * commands from the MATE Service.
 * This means that this test can last as long as needed by the MATE Service, or until the AUT
 * crashes (since they run in the same process).
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class DynamicTest {

    /**
     * Flag indicating whether the DynamicTest should keep listening for commands from the MATE
     * Service.
     */
    public static volatile boolean keepRunning = true;

    /**
     * Launches Main activity of the target package.
     * This method executes before the DynamicTest#run method below.
     * @throws Exception if no Main activity is found for the target package
     */
    @Before
    public void setup() throws Exception {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.setInTouchMode(true);

        // Which package name are we targeting?
        Context context = instrumentation.getTargetContext();
        String targetPackageName = context.getPackageName();

        // Launch main activity of the target package
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(targetPackageName);
        if (intent == null) {
            throw new Exception(String.format("No Main Activity was found for package %s", targetPackageName));
            // If facing this error, refer to: https://developer.android.com/reference/android/content/pm/PackageManager#getLaunchIntentForPackage(java.lang.String)
        }

        instrumentation.startActivitySync(intent);
        instrumentation.waitForIdleSync();
    }

    /**
     * The main method of this DynamicTest.
     *
     * First, it tries to establish a connection (binding) with the MATE Service.
     * Then, it loops as needed to keep this thread busy. The MATE Service requests are handled
     * in a different thread.
     * Finally, if the MATE Service indicates that the Representation Layer should exit, this
     * method triggers the teardown of the previously made connection.
     *
     * @throws Exception if it is unable to establish a connection with the MATE Service.
     */
    @Test
    public void run() throws Exception {
        MATEServiceConnection.establish();

        // stay here forever
        while (keepRunning) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        MATEServiceConnection.tearDownIfConnected();
    }
}