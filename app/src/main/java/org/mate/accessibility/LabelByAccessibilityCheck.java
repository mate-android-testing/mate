package org.mate.accessibility;

import org.mate.ui.Widget;

/**
 * Created by marceloeler on 26/06/17.
 */

public class LabelByAccessibilityCheck implements IWidgetAccessibilityCheck{

    public boolean check(Widget widget) {
        if (widget.isEditable() && widget.getLabeledBy()!=null &&  widget.getLabeledBy().equals(""))
            return false;
        return true;
    }
}
