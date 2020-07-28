package org.mate.exploration.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.projection.MediaProjectionManager;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityWindowInfo;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.IAccessibilityViolationChecker;
import org.mate.accessibility.check.wcag.AccessibilityViolationCheckerWCAG;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.view.KeyEvent.KEYCODE_TAB;

public class CheckCurrentScreen {

    public void scanScreen(){

        IAccessibilityViolationChecker wcagChecker = new AccessibilityViolationCheckerWCAG();

        IScreenState screenState = MATE.uiAbstractionLayer.getLastScreenState();

        UiDevice device = MATE.device;
       // device.click(device.getDisplayWidth()/2,device.getDisplayHeight()/2);
       // device.click(device.getDisplayWidth()/2,device.getDisplayHeight()/2);
       // device.click(device.getDisplayWidth()/2,device.getDisplayHeight()/2);
        //device.pressKeyCode(KEYCODE_TAB);
        //device.pressKeyCode(KEYCODE_TAB);

        //device.pressKeyCode(KeyEvent.KEYCODE_MEDIA_PAUSE);
      //  MATE.log("OK media play: " + device.pressKeyCode(KeyEvent.KEYCODE_MEDIA_PLAY));
        MATE.log("OK media play: " + device.pressKeyCode(KeyEvent.KEYCODE_VOLUME_UP));



        //device.pressKeyCode(KeyEvent.KEYCODE_VOLUME_UP);
        //device.pressDPadCenter();

/*
        List<AccessibilityWindowInfo> accWindows = getInstrumentation().getUiAutomation().getWindows();
        int count = 0;
        String showStr = "";
        for (AccessibilityWindowInfo accWindowInfo: accWindows){
            count++;
            if (accWindowInfo.isActive()){

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (accWindowInfo.getTitle()!=null)
                        showStr += accWindowInfo.getTitle().toString();
                }
                showStr += "  is focused: " + accWindowInfo.isAccessibilityFocused();
            }
        }

        showStr += "   count: " + count;
        MATE.log(showStr);
/*
        Context targetContext = getInstrumentation().getTargetContext();
        AudioManager audioManager = (AudioManager) targetContext.getSystemService(Context.AUDIO_SERVICE);
        MATE.log("Sound active: " +audioManager.isMusicActive());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<AudioPlaybackConfiguration> configs = audioManager.getActivePlaybackConfigurations();
            for (AudioPlaybackConfiguration config: configs){
                AudioAttributes audio = config.getAudioAttributes();
                MATE.log(" content type audio: " + audio.getContentType());
                MATE.log(" audio flag: " + audio.getFlags());
                MATE.log( " audio usage: " + audio.getUsage());
                MATE.log("  audio volume: " + audio.getVolumeControlStream());
                MATE.log(" volume for sure: " + audioManager.getStreamVolume(audio.getVolumeControlStream()));
                MATE.log(" max volume for sure: " + audioManager.getStreamMaxVolume(audio.getVolumeControlStream()));
                if (audio.getContentType()==AudioAttributes.CONTENT_TYPE_MOVIE){
                    MATE.log(" Playing movie");
                }
            }
        }
        MATE.log("Volume fixed: " + audioManager.isVolumeFixed());
        MediaSessionManager mediaManager = (MediaSessionManager) targetContext.getSystemService(Context.MEDIA_SESSION_SERVICE);

        AccessibilityManager accServiceManager = (AccessibilityManager) targetContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accServices = accServiceManager.getInstalledAccessibilityServiceList();
        MATE.log("ACC SERVICES: " + accServices.size());
        for (AccessibilityServiceInfo accService: accServices){
            MATE.log("ID: " + accService.getId());
        }


*/

        //encontrar um botão que quando clicado altera o volume, ou pausa o vídeo
        //


        for (Widget widget: screenState.getWidgets()){
            MATE.log(widget.getId() + " " + widget.getClazz() + " " + widget.getText() + " ");
        }

        Registry.getEnvironmentManager().screenShot(screenState.getPackageName(), screenState.getId());

        MATE.log("Current screen state: " + screenState.getId());


        List<AccessibilityViolation> violations = new ArrayList<AccessibilityViolation>();

        violations = wcagChecker.runAccessibilityChecks(screenState);

        MATE.log_acc("Amount of violations found: " + violations.size());
        for (AccessibilityViolation violation: violations){
            MATE.log_acc(violation.getType() + " " + violation.getWidget().getId() + ":"+violation.getWidget().getText()+ " -- " + violation.getInfo());
        }

        MATE.log("END OF CURRENT SCREEN VALIDATION");

        //Context appContext = getInstrumentation().getContext().getApplicationContext();
        //Context generalContext = getInstrumentation().getContext();
        //Context targetContext = getInstrumentation().getTargetContext();


        //AudioManager audioManager = (AudioManager) targetContext.getSystemService(Context.AUDIO_SERVICE);
        //MATE.log("Sound active: " +audioManager.isMusicActive());

        //ActivityManager activityManager = (ActivityManager) targetContext.getSystemService(Context.ACTIVITY_SERVICE);


        //DisplayManager displayManager = (DisplayManager) targetContext.getSystemService(Context.DISPLAY_SERVICE);
        //MATE.log("Length: " + displayManager.getDisplays().length);
        //Display display = displayManager.getDisplays()[0];
        //MATE.log("density: " + display.getName());
        //DisplayMetrics dm = new DisplayMetrics();
        //display.getMetrics(dm);
        //MATE.log("density: " + dm.density);




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


/*


        try {
            //MATE.log("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            MATE.log("WAIT WAIT WAIT");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        WidgetAction manualAction = new WidgetAction(ActionType.MANUAL_ACTION);

        MATE.uiAbstractionLayer.executeAction(manualAction);
        IScreenState previousScreenState = screenState;
        screenState = MATE.uiAbstractionLayer.getLastScreenState();

        if (!screenState.getPackageName().equals(currentPackageName)){
            AccessibilityViolation violation = new AccessibilityViolation(AccessibilityViolationType.LINK_TO_ALTERNATIVE_FORMAT,manualAction.getWidget(), previousScreenState,"");
            violation.setWarning(true);
        }

        AccessibilityViolationChecker.runAccessibilityChecks(screenState);



        //ContentResizingAccessibilityCheck contentResizingAccessibilityCheck = new ContentResizingAccessibilityCheck();
        //contentResizingAccessibilityCheck.check(screenState);


        //MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(screenState);
        //TextContrastRatioAccessibilityCheck contrastChecker = new TextContrastRatioAccessibilityCheck(screenState.getPackageName(),screenState.getActivityName(),screenState.getId(),device
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


    }
}
