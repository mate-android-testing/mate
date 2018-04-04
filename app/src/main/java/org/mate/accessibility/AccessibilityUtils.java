package org.mate.accessibility;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.eyesfree.utils.AccessibilityNodeInfoUtils;

import org.mate.ui.Widget;

/**
 * Created by marceloeler on 26/07/17.
 */

public class AccessibilityUtils {

    public static boolean checkIfExecutable(AccessibilityNodeInfo node){
        if (node==null){
            return false;
        }
        boolean isClickable = false;

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

    public String getIdFromAccessibityNodeInfo(AccessibilityNodeInfo obj){
        AccessibilityNodeInfo parent = obj.getParent();
        String parentResourceId = this.getValidResourceIDFromTree(parent);

        String id = obj.getViewIdResourceName();
        if (id==null)
            id="";


//        if (id.contains("go_to_help")){
//            UiObject2 obj2 = device.findObject(By.res(id));
//        }


        String clazz = "null";
        if (obj.getClassName()!=null)
            clazz = obj.getClassName().toString();

        String text = "";
        if (obj.getText()!=null)
            text = obj.getText().toString();


        String newId = clazz;
        if (id.equals("")) {

            if (parent!=null && !parentResourceId.equals("")){

                id = parentResourceId+"-child-"+parent.getChildCount()+":"+clazz;
            }
            else
                id = clazz+"-"+text;
        }

        return id;
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
