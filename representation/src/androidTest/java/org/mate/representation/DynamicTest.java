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

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DynamicTest {

    public static volatile boolean keepRunning = true;

    /**
     * Launches Main activity of target package.
     * @throws Exception if no Main activity is found for target package
     */
    @Before
    public void setup() throws Exception {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.setInTouchMode(true);

        // Which package name are we targeting?
        Context context = instrumentation.getTargetContext();
        String targetPackageName = context.getPackageName();

        // Launch main activity of target package
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(targetPackageName);
        if (intent == null) {
            throw new Exception(String.format("No Main Activity was found for package %s", targetPackageName));
            // If facing this error, refer to: https://developer.android.com/reference/android/content/pm/PackageManager#getLaunchIntentForPackage(java.lang.String)
        }

        instrumentation.startActivitySync(intent);
        instrumentation.waitForIdleSync();
    }

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
    }
}