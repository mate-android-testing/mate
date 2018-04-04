package org.mate.accessibility;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityInfoCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityInfoHierarchyCheck;

import org.mate.MATE;
import org.mate.accessibility.results.AccessibilitySummary;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset.LATEST;
import static com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset.getInfoChecksForPreset;

/**
 * Created by marceloeler on 26/07/17.
 */

public class AccessibilityInfoChecker {



    public void runAccessibilityTests(IScreenState state){
        Set<AccessibilityInfoHierarchyCheck> checks = getInfoChecksForPreset(LATEST);
        List<AccessibilityInfoCheckResult> results = new LinkedList<AccessibilityInfoCheckResult>();
        for (AccessibilityInfoHierarchyCheck check : checks) {
            try{
                results.addAll(check.runCheckOnInfoHierarchy(state.getRootAccessibilityNodeInfo(),InstrumentationRegistry.getContext()));
            }
            catch(Exception e){
                MATE.log("exception while running check: " + check.toString());
            }
        }
        List<AccessibilityInfoCheckResult> errors = AccessibilityCheckResultUtils.getResultsForType(
                results, AccessibilityCheckResult.AccessibilityCheckResultType.ERROR);
    }

    public static void checkExtratAccessibilityTests(String packageName,IGUIModel guiModel){
        MATE.log("CHECK CONTRAST AND DUPLICATE CONTENT");
        Instrumentation instrumentation =  getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);

        for (IScreenState state: guiModel.getStates()){
            AccessibilitySummary.currentPackageName=state.getPackageName();
            AccessibilitySummary.currentActivityName=state.getActivityName();

            MATE.log("ACC:checking state: " + state.getId());

            MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(state);
            ContrastRatioAccessibilityCheck contrastChecker = new ContrastRatioAccessibilityCheck(state.getPackageName(),state.getActivityName(),state.getId(),device
                    .getDisplayWidth(),device.getDisplayHeight());
            for (Widget widget: state.getWidgets()) {

                boolean contrastRatioOK = contrastChecker.check(widget);
                if (!contrastRatioOK)
                    AccessibilitySummary.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",widget,String.valueOf(contrastChecker.contratio));

                boolean multDescOK = multDescChecker.check(widget);
                if (!multDescOK)
                    AccessibilitySummary.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW",widget,"");

            }
        }

        MATE.logsum("STATES_VISITED_BY_MATE:"+guiModel.getStates().size());
    }

//    String[] etypes = new String[]{"ACCESSIBILIY_SIZE_FLAW","ACCESSIBILIY_CONTRAST_FLAW",
//            "DUPLICATE_SPEAKABLE_TEXT_FLAW","MISSING_SPEAKABLE_TEXT_FLAW",
//            "EDITABLE_CONTENT_DESC_FLAW","DUPLICATE_CLICKABLE_BOUNDS_FLAW",
//            "CLICKABLE_SPAN_FLAW"};
    public static void listFlawsByWidget(String packageName,IGUIModel guiModel){
        for (IScreenState state: guiModel.getStates()) {
            for (Widget widget: state.getWidgets()){
                String widgetStrSummary = "";
                for (int i=0; i<AccessibilitySummary.etypes.length-1; i++){//removes ACC_FLAW (generic)
                    String etype = AccessibilitySummary.etypes[i];
                    boolean hasflaw = hasAccessibilityFlaw(etype,state.getActivityName(),widget);
                    if (hasflaw) {
                        widgetStrSummary += "1,";
                    }
                    else
                        widgetStrSummary+="0,";
                }
                widgetStrSummary = widgetStrSummary.substring(0,widgetStrSummary.length()-1);
                if (widgetStrSummary.contains("1")){
                    //String line = "ACC_FLAW:" + state.getActivityName().replace(packageName,"") +"," + widget.getId().replace(packageName,"") + ","+widget.getText().replace(",","-")+",";
                    //line+=widgetStrSummary;
                    //MATE.log_acc(line);
                    //AccessibilitySummary.addAccessibilityFlaw(state.getActivityName(),"ACC_FLAW",widget,widgetStrSummary);
                }
            }
        }
    }

    private static boolean hasAccessibilityFlaw(String etype, String activityName, Widget widget) {
        Hashtable<String,Set<String>> flawsByType = AccessibilitySummary.flawsByTypeAndActivity.get(etype);
        if (flawsByType != null){
            Set<String> flawsByActivity = flawsByType.get(activityName);
            if (flawsByActivity!=null){
                if (flawsByActivity.contains(widget.getId()))
                    return true;
            }
        }
        return false;
    }
}
