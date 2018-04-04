package org.mate.accessibility.results;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityUtils;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

/**
 * Created by marceloeler on 26/07/17.
 */

public class AccessibilitySummary {

    public static Hashtable<String,Hashtable<String,Set<String>>> flawsByTypeAndActivity = new Hashtable<String,Hashtable<String,Set<String>>>();

    public static String currentActivityName = "";
    public static String currentPackageName = "";

    public int size=0;
    public int contrast=0;
    public int missingspeakabletext=0;
    public int editablecontentdesc=0;
    public int duplicatespeakabletext=0;
    public int clickablespan=0;
    public int duplicateclickablebounds=0;

    public static String[] etypes = new String[]{"ACCESSIBILITY_SIZE_FLAW","ACCESSIBILITY_CONTRAST_FLAW",
            "DUPLICATE_SPEAKABLE_TEXT_FLAW","MISSING_SPEAKABLE_TEXT_FLAW",
            "EDITABLE_CONTENT_DESC_FLAW","DUPLICATE_CLICKABLE_BOUNDS_FLAW",
            "CLICKABLE_SPAN_FLAW","REDUNDANT_CONTENT_DESC_INFO","ACC_FLAW"};

    public static Vector<String> size_flaws = new Vector<String>();
    public static Vector<String> contrast_flaws = new Vector<String>();
    public static Vector<String> all_flaws = new Vector<String>();

    public static void addAccessibilityFlaw(String checkType, AccessibilityNodeInfo node, String extraInfo){

        String activityName = currentActivityName;
        String packageName = currentPackageName;

        activityName = activityName.replace(packageName,"");
        activityName = activityName.replace("/.","");
        Hashtable<String,Set<String>> flawsByType = flawsByTypeAndActivity.get(checkType);
        if (flawsByType == null){
            flawsByType = new Hashtable<String,Set<String>>();
            flawsByTypeAndActivity.put(checkType,flawsByType);
        }

        Set<String> flawsByActivity = flawsByType.get(activityName);
        if (flawsByActivity==null){
            flawsByActivity = new HashSet<String>();
            flawsByType.put(activityName,flawsByActivity);

        }

        String nodeId = AccessibilityUtils.getValidResourceIDFromTree(node);
        if (!flawsByActivity.contains(nodeId)){
            flawsByActivity.add(nodeId);
            flawsByType.put(activityName,flawsByActivity);

            reportFlaw(packageName,activityName,checkType,node,extraInfo);
        }
    }

    public static void addAccessibilityFlaw(String checkType, Widget node, String extraInfo){

        String activityName = currentActivityName;
        String packageName = currentPackageName;

        Hashtable<String,Set<String>> flawsByType = flawsByTypeAndActivity.get(checkType);
        if (flawsByType == null){
            flawsByType = new Hashtable<String,Set<String>>();
            flawsByTypeAndActivity.put(checkType,flawsByType);
        }

        Set<String> flawsByActivity = flawsByType.get(activityName);
        if (flawsByActivity==null){
            flawsByActivity = new HashSet<String>();
            flawsByType.put(activityName,flawsByActivity);
        }

        if (!flawsByActivity.contains(node.getId())){
            flawsByActivity.add(node.getId());
            flawsByType.put(activityName,flawsByActivity);
            reportFlaw(packageName,activityName,checkType,node,extraInfo);
        }


    }

    private static void reportFlaw(String packageName, String activityName, String checkType, AccessibilityNodeInfo node, String extraInfo) {

        String nodeId = AccessibilityUtils.getValidResourceIDFromTree(node);
        String text = "";
        if (node.getText()!=null)
            text = node.getText().toString();
        text = text.replace(",","-");

        text = text.replace("\n","#");

        String clazz = "";
        if (node.getClassName()!=null);
            clazz = node.getClassName().toString();


        if (checkType.equals("ACCESSIBILITY_SIZE_FLAW")) {
            size_flaws.add("ACC_SIZE:"+activityName.replace(packageName,"") +","+nodeId.replace(packageName,"")+","+clazz+","+text+","+extraInfo);
            //MATE.log_acc("ACC_SIZE:"+activityName.replace(packageName,"") +","+nodeId.replace(packageName,"")+","+text+","+extraInfo);
        }

        all_flaws.add("ACC_FLAW:"+activityName.replace(packageName,"") + ","+nodeId.replace(packageName,"")+","+clazz+","+text.replace(",","-")+","+checkType+","+extraInfo);
        //MATE.log_acc("ACC_FLAW:"+activityName.replace(packageName,"") + ","+widget.getId().replace(packageName,"")+","+text.replace(",","-")+","+extraInfo);


        MATE.log(checkType+" - " + nodeId + " - " + text);
    }

    private static void reportFlaw(String packageName, String activityName, String checkType, Widget widget, String extraInfo) {
        String text=  widget.getText();
        text = text.replace("\n","#");
        if (checkType.equals("ACCESSIBILITY_CONTRAST_FLAW")) {
            contrast_flaws.add("ACC_CONTRAST:"+activityName.replace(packageName,"") + ","+widget.getId().replace(packageName,"")+","+widget.getClazz()+","+text.replace(",","-")+","+extraInfo);
            //MATE.log_acc("ACC_CONTRAST:"+activityName.replace(packageName,"") + ","+widget.getId().replace(packageName,"")+","+text.replace(",","-")+","+extraInfo);
        }

        all_flaws.add("ACC_FLAW:"+activityName.replace(packageName,"") + ","+widget.getId().replace(packageName,"")+","+widget.getClazz()+","+text.replace(",","-")+","+checkType+","+extraInfo);

        MATE.log(checkType+" - " + widget.getClazz() + " - " + widget.getId() + " - "+ widget.getText());
    }

    public static void printSummary(IGUIModel guiModel){

        for (String accflaw: size_flaws){
            MATE.log_acc(accflaw);
        }

        for (String accflaw: contrast_flaws){
            MATE.log_acc(accflaw);
        }

        for (String accflaw: all_flaws){
            MATE.log_acc(accflaw);
        }

        int flawscount[] = new int[8];
        String summaryStr = "";
        int total=0;
        for (int i=0; i<etypes.length-1; i++){//exclude acc_flaws
            String etype = etypes[i];
            int cont = 0;
            Hashtable<String,Set<String>> flawsByType = flawsByTypeAndActivity.get(etype);
            if(flawsByType!=null){
                for (String activity: flawsByType.keySet()){
                    Set<String> flawsByActivity = flawsByType.get(activity);
                    cont+=flawsByActivity.size();
                }
            }
            MATE.logsum(etype.replace("_"," ") + ": " + cont);
            summaryStr+=String.valueOf(cont)+",";
            flawscount[i]=cont;
            total+=cont;
        }
        if (summaryStr.length()>0)
            summaryStr = summaryStr.substring(0,summaryStr.length()-1);

        //size,contrast,missingspeakabletext,editablecontentdesc,duplicatespeakabletext,clickablespan,duplicateclickablebounds;

//        MATE.log("Accessibility problems:");
//        MATE.log(" ... size: " + sizeFlaws);
//        MATE.log(" ... content desc: " + contentDescFlaws);
//        MATE.log(" ... labeled by: " + labeledByFlaws);
//        MATE.log(" ... contrastratio: " + contrastFlaws);
//        MATE.log(" ... mult desc: " + multDescFlaws);
          MATE.logsum("ACC_SUMMARY:"+currentPackageName+","+total+","+summaryStr);

        MATE.logsum("STATES_VISITED_BY_MATE:"+guiModel.getStates().size());
        Set<String> uniqueActivities = new HashSet<String>();
        for (IScreenState state: guiModel.getStates()){
            if (state.getActivityName().contains(currentPackageName))
                uniqueActivities.add(state.getActivityName());
        }

        MATE.logsum("ACTIVITIES_VISITED_BY_MATE:"+uniqueActivities.size());
        for (String st: uniqueActivities){
             MATE.logsum("activity: " + st);
        }
    }


}
