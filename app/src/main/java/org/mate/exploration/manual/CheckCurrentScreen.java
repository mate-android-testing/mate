package org.mate.exploration.manual;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.support.test.uiautomator.UiDevice;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolationChecker;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class CheckCurrentScreen {

    public void scanScreen(){

        Context context = getInstrumentation().getContext();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        MATE.log("Sound active: " +audioManager.isMusicActive());
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        /*
        Instrumentation instrumentation =  getInstrumentation();
        instrumentation = getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);
        String packageName = device.getCurrentPackageName();

        //list all activities of the application being executed
        PackageManager pm = (PackageManager) context.getPackageManager();
        try {
            PackageInfo pinfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activities = pinfo.activities;
            for (int i = 0; i < activities.length; i++) {
                MATE.log("Activity " + (i + 1) + ": " + activities[i].name);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        Context context = InstrumentationRegistry.getTargetContext();
        AccessibilityManager accMger = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        MATE.log("AccMger is enabled: " + accMger.isEnabled());
        MATE.log("AccMger is touch exp enabled: " + accMger.isTouchExplorationEnabled());

        List<AccessibilityServiceInfo> accServices = accMger.getInstalledAccessibilityServiceList();
        for (AccessibilityServiceInfo accInfo: accServices){
            MATE.log(accInfo.getId() + " " + accInfo.getSettingsActivityName());
        }

        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        List<AccessibilityWindowInfo> windowsInfo = uiAutomation.getWindows();

        AccessibilityNodeInfo ani = uiAutomation.getRootInActiveWindow();

        AudioManager audioManager = (AudioManager) getInstrumentation().getContext().getSystemService(Context.AUDIO_SERVICE);
        MATE.log("Sound active: " +audioManager.isMusicActive());


        if (ani!=null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                MATE.log("--" + String.valueOf(ani.getPaneTitle()) + " - " + ani.getViewIdResourceName());
                MATE.log(String.valueOf(ani.getChildCount()));
            }
            for (AccessibilityWindowInfo awi : windowsInfo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    MATE.log(String.valueOf(awi.getTitle()));
                }

            }
        }

        */

        IScreenState screenState = MATE.uiAbstractionLayer.getLastScreenState();

        Registry.getEnvironmentManager().screenShot(screenState.getPackageName(), screenState.getId());

        MATE.log("Current screen state: " + screenState.getId());

        for (Widget w : screenState.getWidgets()) {
            //MATE.log(w.getId() + " " + w.getClazz() + "  IFA: " + w.isImportantForAccessibility() + " actionable: " + w.isActionable() + " icc: " + w.isContextClickable() + " clickable: " + w.isClickable());
            //if (w.getParent()!=null)
            //MATE.log("------ son of " + w.getParent().getClazz());
            //if (w.isEditable())
            //MATE.log("INPUT TYPE: " + w.getInputType());
            //MATE.log("\n");
            //MATE.log("");
        }

        AccessibilityViolationChecker.runAccessibilityChecks(screenState);

        try {
            //MATE.log("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            MATE.log("WAIT WAIT WAIT");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        screenState = ScreenStateFactory.getScreenState("ActionsScreenState");

        AccessibilityViolationChecker.runAccessibilityChecks(screenState);



        //ContentResizingAccessibilityCheck contentResizingAccessibilityCheck = new ContentResizingAccessibilityCheck();
        //contentResizingAccessibilityCheck.check(screenState);


        //MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(screenState);
        //ContrastRatioAccessibilityCheck contrastChecker = new ContrastRatioAccessibilityCheck(screenState.getPackageName(),screenState.getActivityName(),screenState.getId(),device
          //      .getDisplayWidth(),device.getDisplayHeight());
        /*
        FormControlLabelCheck formCheck = new FormControlLabelCheck();
        for (Widget widget: screenState.getWidgets()) {

            //boolean contrastRatioOK = contrastChecker.check(widget);
            //MATE.log("Check contrast of "+widget.getId() + ": " + contrastChecker.contratio);

            //if (!contrastRatioOK)
              //  AccessibilitySummaryResults.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",widget,String.valueOf(contrastChecker.contratio));

            //boolean multDescOK = multDescChecker.check(widget);
            //if (!multDescOK)
                //AccessibilitySummaryResults.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW",widget,"");

            boolean formLabelOK = formCheck.check(screenState, widget);
            if (!formLabelOK) {
                MATE.log("FORM CONTROL LABEL ERROR: " + " - " + widget.getClazz() + " - " + widget.getId() + " - " + widget.getText());
            }

        }*/

        MATE.log("END OF CURRENT SCREEN VALIDATION");
    }
}
