package org.mate;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.interaction.DeviceMgr;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.Date;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;


/**
 * Created by marceloeler on 29/06/17.
 */
@RunWith(AndroidJUnit4.class)
public class CheckScreen {

    private DeviceMgr deviceMgr;


    @Test
    public void useAppContext() throws Exception {


        MATE.log("start checkscreenstate");

        /*
        Thread.sleep(5000);

        Context context = getInstrumentation().getContext();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        MATE.log("Sound active: " +audioManager.isMusicActive());
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        Instrumentation instrumentation =  getInstrumentation();
        instrumentation = getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);
        String packageName = device.getCurrentPackageName();

        //list all activities of the application being executed
        PackageManager pm = (PackageManager) context.getPackageManager();
        try {
            PackageInfo pinfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            MATE.log("ACITIVY NOW: " + cn);

            ActivityInfo[] activities = pinfo.activities;
            for (int i = 0; i < activities.length; i++) {
                MATE.log("Activity " + (i + 1) + ": " + activities[i].name);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        */

        MATE mate = new MATE();
        //run mate for timeout minutes
        mate.testApp("checkScreen");
    }

}
