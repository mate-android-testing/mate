package org.mate.accessibility;

import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class AccessibilityViolation {

    private String type;
    private Widget widget;
    private IScreenState state;
    private String info;

    public AccessibilityViolation(String type, Widget widget, IScreenState state, String info) {
        this.type = type;
        this.widget = widget;
        this.state = state;
        this.info = info;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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
}
