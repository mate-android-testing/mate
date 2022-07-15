package org.mate;

import androidx.test.runner.AndroidJUnit4;

import org.junit.runner.RunWith;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.manual.CheckCurrentScreen;
import org.mate.interaction.DeviceMgr;


/**
 * Created by marceloeler on 29/06/17.
 */
@RunWith(AndroidJUnit4.class)
public class CheckScreen {

    private DeviceMgr deviceMgr;


    public void run(String packageName) throws Exception {


        MATELog.log("start checkscreenstate");

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

        CheckCurrentScreen checkScreen = new CheckCurrentScreen();
        checkScreen.scanScreen();
    }

}
