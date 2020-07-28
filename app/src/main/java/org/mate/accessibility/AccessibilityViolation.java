package org.mate.accessibility;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class AccessibilityViolation {

    private AccessibilityViolationType type;
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
        String checkType = type.getValue();
        String packageName = state.getPackageName();
        String activityName = state.getActivityName();
        String stateId = state.getId();
        String widgetid = widget.getId();
        widgetid = widgetid.replace(":","/");
        widgetid = widgetid.replace(",","-");
        info = info.replace(":","- ");
        String widgetText = widget.getText();
        widgetText = widgetText.replace(":","-");
        widgetText = widgetText.replace(",",".");
        widgetText = widgetText.replace("\n","  ");
        String extraInfo = info;

        StringBuilder flawMessage = new StringBuilder();
        flawMessage.append(packageName).append(":");
        flawMessage.append(MATE.sessionID).append(":");
        flawMessage.append(activityName).append(":");
        flawMessage.append(stateId).append(":");
        flawMessage.append(checkType).append(":");
        flawMessage.append(widget.getClazz()).append(":");
        flawMessage.append(widgetid).append(":");
        flawMessage.append(widgetText).append(":");
        flawMessage.append(extraInfo).append(":");
        flawMessage.append(widget.getX1()).append(":");
        flawMessage.append(widget.getY1()).append(":");
        flawMessage.append(widget.getX2()).append(":");
        flawMessage.append(widget.getY2()).append(":");

        //String flawMsg = packageName+":"+sessionID+activityName+":"+stateId+":"+checkType+":" + widget.getClazz() + ":" + widgetid + ":"+ widgetText;
        //flawMsg+=":"+extraInfo+":"+widget.getX1()+":"+widget.getY1()+":"+widget.getX2()+":"+widget.getY2();
        Registry.getEnvironmentManager().sendFlawToServer(flawMessage.toString());
    }

    public AccessibilityViolation(AccessibilityViolationType type, Widget widget, IScreenState state, String info) {
        this.type = type;
        this.widget = widget;
        this.state = state;
        this.info = info;
        this.warning = false;
        //reportFlaw(type,widget,state,info);
    }

    public AccessibilityViolation(AccessibilityViolationType type, Widget widget, boolean warning){
        this.type = type;
        this.widget = widget;
        this.warning = warning;
        this.state = null;
        this.info = "";
        //reportFlaw(type,widget,state,info);

    }

    public AccessibilityViolation(AccessibilityViolationType type, IScreenState state, String info) {
        this.type = type;
        this.widget = null;
        this.state = state;
        this.info = info;
        this.warning = false;
        //reportFlaw(type,widget,state,info);
    }

    public AccessibilityViolationType getType() {
        return type;
    }

    public void setType(AccessibilityViolationType type) {
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
