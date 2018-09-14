package org.mate.state.executables;

import android.app.Instrumentation;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.StaleObjectException;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.view.accessibility.AccessibilityNodeInfo;

import android.content.Context;

import org.mate.MATE;
import org.mate.ui.Widget;
import org.mate.ui.EnvironmentManager;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by marceloeler on 21/06/17.
 */

public class AppScreen {

    private String activityName;
    private String packageName;
    private Vector<Widget> widgets;
    private static Hashtable<String,String> editTextHints = new Hashtable<String,String>();
    private UiDevice device;
    private boolean hasToScrollUp;
    private boolean  hastoScrollDown;
    private boolean  hasToScrollLeft;
    private boolean  hasToScrollRight;
    private AccessibilityNodeInfo rootNodeInfo;

    public AppScreen(){
        this.widgets = new Vector<Widget>();
        this.activityName = EnvironmentManager.getCurrentActivityName();

        Instrumentation instrumentation =  getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        this.packageName = device.getCurrentPackageName();
        AccessibilityNodeInfo ninfo= InstrumentationRegistry.getInstrumentation().getUiAutomation().getRootInActiveWindow();
        if (ninfo==null) {
            MATE.log("APP DISCONNECTED");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getContext());
            if (prefs.getBoolean("isServiceEnabled", false)){
                MATE.log("ACCSERVICE FALSE");
            }
            else
                MATE.log("ACCSERVICE TRUE");
            //Try to reconnect
        }
        rootNodeInfo = ninfo;
        readNodes(ninfo,null);
    }

    public void readNodes(AccessibilityNodeInfo obj, Widget parent){
        Widget widget = null;
        if (obj!=null) {
            try {
                widget = createWidget(obj, parent, activityName);
            }
            catch(StaleObjectException e){
                MATE.log("StaleObjectException");
            }
            if (widget!=null)
                widgets.add(widget);
            else{
                Rect rec = new Rect();
                obj.getBoundsInScreen(rec);
                //MATE.log("WIDGET NULL: "+ rec.toShortString());



            }
            for (int i=0; i<obj.getChildCount(); i++)
                readNodes(obj.getChild(i),widget);
        }
    }

    private Widget createWidget(AccessibilityNodeInfo obj, Widget parent, String activityName){
        String parentResourceId = this.getValidResourceIDFromTree(parent);



        String id = obj.getViewIdResourceName();
        if (id==null)
            id="";

        String clazz = "null";
        if (obj.getClassName()!=null)
            clazz = obj.getClassName().toString();

        String text = "";
        if (obj.getText()!=null)
            text = obj.getText().toString();

        String newId = clazz;
        if (id.equals("")) {

            if (parent!=null && !parentResourceId.equals("")){

                id = parentResourceId+"-child-"+parent.getChildren().size()+":"+clazz;
            }
            else
                id = clazz+"-"+text;
        }

        String idByActivity = activityName+"_"+id;
        Widget widget = new Widget(id, clazz, idByActivity);

        String res = obj.getViewIdResourceName();
        if (res==null)
            res="";
        widget.setResourceID(res);

        widget.setParent(parent);
        widget.setText(text);
        String wpackageName = "null";
        if (obj.getPackageName()!=null)
            wpackageName = obj.getPackageName().toString();
        widget.setPackageName(wpackageName);
        widget.setEnabled(obj.isEnabled());

        Rect rec = new Rect();
        obj.getBoundsInScreen(rec);
        widget.setBounds(rec.toShortString());

        int x1=widget.getX1();
        int x2=widget.getX2();
        int y1=widget.getY1();
        int y2=widget.getY2();
        if (x1<0 || x2<0){
            this.hasToScrollLeft=true;
            return null;
        }
        if (x2>device.getDisplayWidth() || x1>device.getDisplayWidth()) {
            this.hasToScrollRight = true;
            return null;
        }
        if (y1<0 || y2<0) {
            this.hasToScrollUp = true;
            return null;
        }
        if (y2>device.getDisplayHeight()||y1>device.getDisplayHeight()) {
            this.hastoScrollDown = true;
            return null;
        }

        widget.setCheckable(obj.isCheckable());
        widget.setChecked(obj.isChecked());
        widget.setClickable(obj.isClickable());
        String contentDesc = "";
        if (obj.getContentDescription()!=null)
            contentDesc = obj.getContentDescription().toString();
        if (contentDesc==null)
            contentDesc="";
        widget.setContentDesc(contentDesc);
        widget.setFocusable(obj.isFocusable());
        widget.setHasChildren(obj.getChildCount()!=0);
        widget.setIndex(0);
        widget.setLongClickable(obj.isLongClickable());
        widget.setPassword(false);
        widget.setScrollable(obj.isScrollable());
        widget.setSelected(obj.isSelected());
        widget.setMaxLength(obj.getMaxTextLength());
        widget.setInputType(obj.getInputType());

        AccessibilityNodeInfo lb = obj.getLabeledBy();
        if (lb!=null){
            String lbstr = lb.getViewIdResourceName();
            if (lbstr==null)
                lbstr = "";
            widget.setLabeledBy(lbstr);
        }



        if (widget.isEditable()){

            String hint = editTextHints.get(id);

            if (hint==null){
                UiObject2 uiobject = null;
                if (widget.getResourceID().equals(""))
                    uiobject = device.findObject(By.text(widget.getText()));
                else
                    uiobject = device.findObject(By.res(id));
                if (uiobject!=null) {
                    String textBeforeClear = uiobject.getText();
                    if (textBeforeClear==null)
                        textBeforeClear="";

                    uiobject.setText("");
                    String textAfterClear = uiobject.getText();

                    if (textAfterClear==null)
                        textAfterClear="";
                    uiobject.setText(textBeforeClear);

                    hint = textAfterClear;
                    if (!widget.getResourceID().equals(""))
                        editTextHints.put(id,hint);

                    if (textAfterClear.equals(textBeforeClear))
                        uiobject.setText("");
                }
            }
            if (hint==null)
                hint="";
            widget.setHint(hint);
            widget.setContentDesc(hint);


        }
        if (parent!=null)
            parent.addChild(widget);
        return widget;
    }

    private String getValidResourceIDFromTree(Widget obj){
        String rid="";
        while(obj!=null && rid.equals("")){
            if (obj.getResourceID()!=null&&!obj.getResourceID().equals(""))
                rid=obj.getResourceID();
            else
                obj = obj.getParent();
        }
        return rid;
    }

    public String getActivityName() {
        return activityName;
    }

    public Vector<Widget> getWidgets(){
        return widgets;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isHasToScrollUp() {
        return hasToScrollUp;
    }

    public boolean isHastoScrollDown() {
        return hastoScrollDown;
    }

    public boolean isHasToScrollLeft() {
        return hasToScrollLeft;
    }

    public boolean isHasToScrollRight() {
        return hasToScrollRight;
    }

    public AccessibilityNodeInfo getRootNodeInfo(){
        return this.rootNodeInfo;
    }
}
