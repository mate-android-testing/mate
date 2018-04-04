package org.mate.ui;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import org.mate.MATE;
import org.mate.accessibility.AccessibilitySettings;
import org.mate.accessibility.ContentDescAccessibilityCheck;
import org.mate.accessibility.ContrastRatioAccessibilityCheck;
import org.mate.accessibility.LabelByAccessibilityCheck;
import org.mate.accessibility.MultipleContentDescCheck;
import org.mate.accessibility.TargetSizeAccessibilityCheck;
import org.mate.accessibility.results.AccessibilitySummary;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by marceloeler on 04/07/17.
 */

public class AccessibilityChecker {

    private static int sizeFlaws=0;
    private static int contrastFlaws=0;
    private static int labeledByFlaws=0;
    private static int contentDescFlaws=0;
    private static int multDescFlaws=0;

//    public static void checkAccessibility(String packageName,IGUIModel guiModel){
//        Instrumentation instrumentation =  getInstrumentation();
//        UiDevice device = UiDevice.getInstance(instrumentation);
//        AccessibilitySettings.DENSITY=Integer.valueOf(EnvironmentManager.getDensity());
//
//        TargetSizeAccessibilityCheck targetSizeChecker = new TargetSizeAccessibilityCheck();
//        ContentDescAccessibilityCheck contentDescChecker = new ContentDescAccessibilityCheck();
//        LabelByAccessibilityCheck labeledByChecker = new LabelByAccessibilityCheck();
//
//        Hashtable<String,Set<String>> sizeFlawsByActivity = new Hashtable<String,Set<String>>();
//        Hashtable<String,Set<String>> contentDescFlawsByActivity = new Hashtable<String,Set<String>>();
//        Hashtable<String,Set<String>> labeledByFlawsByActivity = new Hashtable<String,Set<String>>();
//        Hashtable<String,Set<String>> contrastFlawsByActivity = new Hashtable<String,Set<String>>();
//        Hashtable<String,Set<String>> multDescFlawsByActivity = new Hashtable<String,Set<String>>();
//
//
//        for (IScreenState state: guiModel.getStates()){
//            MATE.log("ACC:checking state: " + state.getId());
//            MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(state);
//            ContrastRatioAccessibilityCheck contrastChecker = new ContrastRatioAccessibilityCheck(state.getPackageName(),state.getId(),device
//            .getDisplayWidth(),device.getDisplayHeight());
//            Vector<Action> actions = state.getActions();
//            Set<String> ids = new HashSet<String>();
//            for (Widget widget: state.getWidgets()) {
//                    //MATE.log("CHECK " + widget.getId() + " " + widget.getText() + " " + widget.getBounds());
//
//                    boolean sizeOK = true;
//                    Set<String> sizeFlawsSet = sizeFlawsByActivity.get(state.getActivityName());
//                    if (sizeFlawsSet==null){
//                        sizeFlawsSet = new HashSet<String>();
//                    }
//                    if (!sizeFlawsSet.contains(widget.getId())) {
//                        sizeOK = targetSizeChecker.check(widget);
//                    }
//
//                    boolean contentDescOK = true;
//                    Set<String> contentFlawsSet = contentDescFlawsByActivity.get(state.getActivityName());
//                    if (contentFlawsSet==null){
//                        contentFlawsSet = new HashSet<String>();
//                    }
//                    if (!contentFlawsSet.contains(widget.getId())){
//                        contentDescOK = contentDescChecker.check(widget);
//                    }
//
//                    boolean labeledByOK = true;
//                    Set<String> labelFlawsSet = labeledByFlawsByActivity.get(state.getActivityName());
//                    if (labelFlawsSet==null){
//                        labelFlawsSet = new HashSet<String>();
//                    }
//                    if (!labelFlawsSet.contains(widget.getId())) {
//                        labeledByOK = labeledByChecker.check(widget);
//                    }
//
//                    boolean contrastRatioOK = true;
//                    Set<String> contrastFlawsSet = contrastFlawsByActivity.get(state.getActivityName());
//                    if (contrastFlawsSet==null){
//                        contrastFlawsSet =  new HashSet<String>();
//                    }
//                    if (!contrastFlawsSet.contains(widget.getId())) {
//                        contrastRatioOK = contrastChecker.check(widget);
//                    }
//
//                    boolean multDescOK = true;
//                    Set<String> multDescFlawsSet = multDescFlawsByActivity.get(state.getActivityName());
//                    if (multDescFlawsSet==null){
//                        multDescFlawsSet = new HashSet<String>();
//                    }
//                    if (!multDescFlawsSet.contains(widget.getId())){
//                        multDescOK = multDescChecker.check(widget);
//                    }
//
//
//                    if (!sizeOK || !contentDescOK || !labeledByOK || !contrastRatioOK || !multDescOK) {
//                        String line = "ACCESSIBILITY_FLAW:" + packageName+","+state.getActivityName() +"," + widget.getId() + ","+widget.getText().replace(",","-")+","+widget.getBounds().replace(",","-")+",";
//                        if (sizeOK)
//                            line += "0,";
//                        else {
//                            line += "1,";
//                            sizeFlaws++;
//                            MATE.log("ACCESSIBILIY_SIZE_FLAW:"+packageName+","+state.getActivityName() +","+widget.getId()+","+widget.getText().replace(",","-")+","+widget.getBounds().replace(",","-")+","+targetSizeChecker.w+","+targetSizeChecker.h);
//                            sizeFlawsSet.add(widget.getId());
//                            sizeFlawsByActivity.put(state.getActivityName(),sizeFlawsSet);
//                        }
//
//                        if (contentDescOK)
//                            line += "0,";
//                        else {
//                            line += "1,";
//                            contentDescFlaws++;
//                            contentFlawsSet.add(widget.getId());
//                            contentDescFlawsByActivity.put(state.getActivityName(),contentFlawsSet);
//                        }
//
//                        if (labeledByOK)
//                            line += "0,";
//                        else {
//                            line += "1,";
//                            labeledByFlaws++;
//                            labelFlawsSet.add(widget.getId());
//                            labeledByFlawsByActivity.put(state.getActivityName(),labelFlawsSet);
//                        }
//
//                        if (contrastRatioOK)
//                            line += "0,";
//                        else{
//                            line += "1,";
//                            contrastFlaws++;
//                            MATE.log("ACCESSIBILIY_CONTRAST_FLAW:"+packageName+","+state.getActivityName() + ","+widget.getId()+","+widget.getText().replace(",","-")+","+widget.getBounds().replace(",","-")+","+contrastChecker.contratio);
//                            contrastFlawsSet.add(widget.getId());
//                            contrastFlawsByActivity.put(state.getActivityName(),contrastFlawsSet);
//                        }
//
//                        if (multDescOK)
//                            line+="0";
//                        else{
//                            line+="1";
//                            multDescFlaws++;
//                            multDescFlawsSet.add(widget.getId());
//                            multDescFlawsByActivity.put(state.getActivityName(),multDescFlawsSet);
//
//                        }
//
//                        line+=","+targetSizeChecker.w+"x"+targetSizeChecker.h+","+contrastChecker.contratio;
//                        MATE.log(line);
//                        ids.add(widget.getId());
//                    }
//            }
//        }
//        MATE.log("Accessibility problems:");
//        MATE.log(" ... size: " + sizeFlaws);
//        MATE.log(" ... content desc: " + contentDescFlaws);
//        MATE.log(" ... labeled by: " + labeledByFlaws);
//        MATE.log(" ... contrastratio: " + contrastFlaws);
//        MATE.log(" ... mult desc: " + multDescFlaws);
//        MATE.log("ACCESSIBILITY_SUMMARY:"+packageName+","+sizeFlaws+","+contentDescFlaws+","+labeledByFlaws+","+contrastFlaws);
//    }


    public static void checkAccessibility(String packageName,IGUIModel guiModel){
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
                    AccessibilitySummary.addAccessibilityFlaw("ACCESSIBILIY_CONTRAST_FLAW",widget,String.valueOf(contrastChecker.contratio));

                boolean multDescOK = multDescChecker.check(widget);
                if (!multDescOK)
                    AccessibilitySummary.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW",widget,"");

            }
        }
    }
}
