package org.mate.accessibility;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.Registry;
import org.mate.accessibility.utils.AccessibilityUtils;
import org.mate.commons.utils.MATELog;
import org.mate.model.deprecated.graph.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.Widget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * Created by marceloeler on 26/07/17.
 */

public class AccessibilitySummaryResults {

    public static Hashtable<String,Hashtable<String,Set<String>>> flawsByTypeAndActivity = new Hashtable<String,Hashtable<String,Set<String>>>();

    public static String currentActivityName = "";
    public static String currentPackageName = "";

    public int size=0;
    public int contrast=0;

    public static String[] etypes = new String[]{"ACCESSIBILITY_SIZE_FLAW","ACCESSIBILITY_CONTRAST_FLAW",
            "DUPLICATE_SPEAKABLE_TEXT_FLAW","MISSING_SPEAKABLE_TEXT_FLAW",
            "EDITABLE_CONTENT_DESC_FLAW","DUPLICATE_CLICKABLE_BOUNDS_FLAW",
            "CLICKABLE_SPAN_FLAW","REDUNDANT_CONTENT_DESC_INFO","ACC_FLAW"};

    public static List<String> size_flaws = new ArrayList<>();
    public static List<String> contrast_flaws = new ArrayList<>();
    public static List<String> all_flaws = new ArrayList<>();

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

    public static void addAccessibilityFlaw(String checkType, Widget widget, String extraInfo){

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

        if (!flawsByActivity.contains(widget.getId())){
            flawsByActivity.add(widget.getId());
            flawsByType.put(activityName,flawsByActivity);
            reportFlaw(packageName,activityName,checkType,widget,extraInfo);


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
        }

        all_flaws.add("ACC_FLAW:"+activityName.replace(packageName,"") + ","+nodeId.replace(packageName,"")+","+clazz+","+text.replace(",","-")+","+checkType+","+extraInfo);

        MATELog.log(checkType+" - " + nodeId + " - " + text);


        activityName = activityName.replace(packageName+"/","");
        String widgetid = nodeId.replace(packageName+":id/","");
        widgetid = widgetid.replace(":","#");

        String widgetText = text.replace(":","--");
        widgetText = widgetText.replace(",","x");

        extraInfo = extraInfo.replace(",","x");

        if (widgetText.equals(""))
            widgetText=" ";
        if (extraInfo.equals(""))
            extraInfo=" ";

        Rect rec = new Rect();
        node.getBoundsInScreen(rec);

        String value = rec.toShortString();
        value = value.replace("][","|");
        value = value.replace("[","");
        value = value.replace("]","");
        String[] twoPos = value.split("\\|");
        String[] first = twoPos[0].split(",");
        String[] second = twoPos[1].split(",");
        int x1 = Integer.valueOf(first[0]);
        int y1 = Integer.valueOf(first[1]);

        int x2 = Integer.valueOf(second[0]);
        int y2 = Integer.valueOf(second[1]);


        String stateId = Registry.getUiAbstractionLayer().getLastScreenState().getId();
        //INCLUDE COORDINATES
        String flawMsg = packageName+":"+activityName+":"+stateId+":"+checkType+":" + clazz + ":" + widgetid + ":"+ widgetText+":"+extraInfo;
        flawMsg+=":"+x1+":"+y1+":"+x2+":"+y2;
        MATELog.log("SEND FLAW TO SERVER");
        Registry.getEnvironmentManager().sendFlawToServer(flawMsg);

    }

    private static void reportFlaw(String packageName, String activityName, String checkType, Widget widget, String extraInfo) {
        String text=  widget.getText();
        text = text.replace("\n","#");
        if (checkType.equals("ACCESSIBILITY_CONTRAST_FLAW")) {
            contrast_flaws.add("ACC_CONTRAST:"+activityName.replace(packageName,"") + ","+widget.getId().replace(packageName,"")+","+widget.getClazz()+","+text.replace(",","-")+","+extraInfo);
        }

        all_flaws.add("ACC_FLAW:"+activityName.replace(packageName,"") + ","+widget.getId().replace(packageName,"")+","+widget.getClazz()+","+text.replace(",","-")+","+checkType+","+extraInfo);

        MATELog.log(checkType+" - " + widget.getClazz() + " - " + widget.getId() + " - "+ widget.getText());

        activityName = activityName.replace(packageName+"/","");
        String widgetid = widget.getId().replace(packageName+":id/","");
        widgetid = widgetid.replace(":","#");

        String widgetText = widget.getText().replace(":","--");


        widget.getBounds();
        if (widgetText.equals(""))
            widgetText=" ";
        if (extraInfo.equals(""))
            extraInfo=" ";

        String stateId = Registry.getUiAbstractionLayer().getLastScreenState().getId();

        String flawMsg = packageName+":"+activityName+":"+stateId+":"+checkType+":" + widget.getClazz() + ":" + widgetid + ":"+ widgetText;
        flawMsg+=":"+extraInfo+":"+widget.getX1()+":"+widget.getY1()+":"+widget.getX2()+":"+widget.getY2();
        Registry.getEnvironmentManager().sendFlawToServer(flawMsg);


    }

    public static void printSummary(IGUIModel guiModel){

        for (String accflaw: size_flaws){
            MATELog.log_acc(accflaw);
        }

        for (String accflaw: contrast_flaws){
            MATELog.log_acc(accflaw);
        }

        for (String accflaw: all_flaws){
            MATELog.log_acc(accflaw);
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
            MATELog.log_debug(etype.replace("_"," ") + ": " + cont);
            summaryStr+=String.valueOf(cont)+",";
            flawscount[i]=cont;
            total+=cont;
        }
        if (summaryStr.length()>0)
            summaryStr = summaryStr.substring(0,summaryStr.length()-1);

        MATELog.log_debug("ACC_SUMMARY:"+currentPackageName+","+total+","+summaryStr);

        MATELog.log_debug("STATES_VISITED_BY_MATE:"+guiModel.getStates().size());
        Set<String> uniqueActivities = new HashSet<String>();
        for (IScreenState state: guiModel.getStates()){
            if (state.getActivityName().contains(currentPackageName))
                uniqueActivities.add(state.getActivityName());
        }

        MATELog.log_debug("ACTIVITIES_VISITED_BY_MATE:"+uniqueActivities.size());
        for (String st: uniqueActivities){
             MATELog.log_debug("activity: " + st);
        }
    }


}
