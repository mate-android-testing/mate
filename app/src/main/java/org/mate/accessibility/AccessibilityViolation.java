package org.mate.accessibility;

import org.mate.Registry;
import org.mate.accessibility.check.screenbased.IScreenAccessibilityCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class AccessibilityViolation {

    private int type;
    private Widget widget;
    private IScreenState state;
    private String info;
    private boolean warning;

    public void reportFlaw(){
        if (widget==null){
            widget = new Widget("artificialId","artificialClass","artificialIdByActivity");
            widget.setX1(0);
            widget.setY1(0);
            widget.setX2(10);
            widget.setY2(10);
        }
        String checkType = AccessibilityViolationTypes.NAMES[type];
        String packageName = state.getPackageName();
        String activityName = state.getActivityName();
        String stateId = state.getId();
        String widgetid = widget.getId();
        widgetid = widgetid.replace(":","/");
        info = info.replace(":","- ");
        String widgetText = widget.getText();
        String extraInfo = info;

        String flawMsg = packageName+":"+activityName+":"+stateId+":"+checkType+":" + widget.getClazz() + ":" + widgetid + ":"+ widgetText;
        flawMsg+=":"+extraInfo+":"+widget.getX1()+":"+widget.getY1()+":"+widget.getX2()+":"+widget.getY2();
        Registry.getEnvironmentManager().sendFlawToServer(flawMsg);
    }

    public AccessibilityViolation(int type, Widget widget, IScreenState state, String info) {
        this.type = type;
        this.widget = widget;
        this.state = state;
        this.info = info;
        this.warning = false;
        //reportFlaw(type,widget,state,info);
    }

    public AccessibilityViolation(int type, Widget widget, boolean warning){
        this.type = type;
        this.widget = widget;
        this.warning = warning;
        this.state = null;
        this.info = "";
        //reportFlaw(type,widget,state,info);

    }

    public AccessibilityViolation(int type, IScreenState state, String info) {
        this.type = type;
        this.widget = null;
        this.state = state;
        this.info = info;
        this.warning = false;
        //reportFlaw(type,widget,state,info);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    public IScreenState getState() {
        return state;
    }

    public void setState(IScreenState state) {
        this.state = state;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }
}
