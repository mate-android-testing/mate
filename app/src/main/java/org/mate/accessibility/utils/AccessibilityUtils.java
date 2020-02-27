package org.mate.accessibility.utils;

import android.view.accessibility.AccessibilityNodeInfo;


/**
 * Created by marceloeler on 26/07/17.
 */

public class AccessibilityUtils {

    public static boolean checkIfExecutable(AccessibilityNodeInfo node){
        if (node==null){
            return false;
        }

        if (node.isClickable() || node.isEditable() || node.isLongClickable() || node.isScrollable())
            return true;

        if (directSonOf(node,"ListView")||directSonOf(node,"GridView")) {
            return true;
        }

        String contentDesc="";
        if (node.getContentDescription()!=null)
            contentDesc = node.getContentDescription().toString();
        String text = "";
        if (node.getText()!=null)
            text = node.getText().toString();
        if (node.getClassName().toString().equals("android.view.View")&&
                (!contentDesc.equals("")||!text.equals(""))){
            return true;
        }

        if (node.getClassName().toString().contains("Spinner")){
            return true;
        }

        return false;
    }

    public static boolean directSonOf(AccessibilityNodeInfo node, String type){
        AccessibilityNodeInfo wparent = node.getParent();
        if (wparent!=null)
            if (wparent.getClassName().toString().contains(type))
                return true;
        return false;
    }

    public static String getValidResourceIDFromTree(AccessibilityNodeInfo obj){
        if (obj==null)
            return "";
        String rid="";


        while(obj!=null && rid.equals("")){
            if (obj.getViewIdResourceName()!=null && !obj.getViewIdResourceName().equals(""))
                rid=obj.getViewIdResourceName();
            else
                obj = obj.getParent();
        }
        return rid;
    }
}
