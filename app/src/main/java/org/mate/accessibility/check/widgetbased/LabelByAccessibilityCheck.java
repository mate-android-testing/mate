package org.mate.accessibility.check.widgetbased;

import org.mate.accessibility.check.widgetbased.IWidgetAccessibilityCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

/**
 * Created by marceloeler on 26/06/17.
 */

public class LabelByAccessibilityCheck implements IWidgetAccessibilityCheck {

    public boolean check(IScreenState state, Widget widget) {
        if (widget.isEditable() && widget.getLabeledBy()!=null &&  widget.getLabeledBy().equals(""))
            return false;
        return true;
    }

    @Override
    public String getType() {
        return "FORM CONTROL LABEL";
    }

    @Override
    public String getInfo() {
        return "";
    }
}
