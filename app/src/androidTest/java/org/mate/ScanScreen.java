package org.mate;

import android.app.Instrumentation;
import android.bluetooth.BluetoothClass;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityWindowInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.accessibility.ContrastRatioAccessibilityCheck;
import org.mate.interaction.DeviceMgr;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.ActionType;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

import java.util.Date;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by marceloeler on 29/06/17.
 */
@RunWith(AndroidJUnit4.class)
public class ScanScreen {

    @Test
    public void useAppContext() throws Exception {
        IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");


        MATE.log("ALL WIDGETS: ");
        for (Widget w: screenState.getWidgets()){
            MATE.log(w.getId() + " - " + w.getClazz() + " - "+ w.getText());
        }
        MATE.log("");
        MATE.log("");
        MATE.log("ACTIONS: ");
        int size =  screenState.getWidgets().size();
        MATE.log("number of widgets: " + size);
        MATE.log(screenState.getActivityName()+", actions: " + screenState.getActions().size());
        Action act=null;
        for (Action action: screenState.getActions()){
            Widget widget = action.getWidget();
            MATE.log("WIDGET ID " + action.getActionType()+" : " + widget.getId() + " txt: " + widget.getText() + " - " + widget.getClazz() + " - " + widget.getBounds());
            if (widget.getParent()!=null) {
                Widget parent = widget.getParent();
                while (parent!=null){
                    MATE.log("   parent: " + parent.getClazz());
                    parent =  parent.getParent();
                }

            }
            if (action.getActionType()>=4 && action.getActionType()<=7)
                act=action;
        }

        Instrumentation instrumentation =  getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);

        DeviceMgr dmgr = new DeviceMgr(device,"book");
//        if (act!=null)
//            dmgr.executeAction(act);
    }


    public Bitmap screenShot() {
        Bitmap bitmap =  InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        return bitmap;
    }
}
