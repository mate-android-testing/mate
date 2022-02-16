package org.mate.accessibility.check.bbc.widgetbased;

import android.graphics.Rect;

import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.accessibility.check.bbc.AccessibilitySettings;
import org.mate.accessibility.check.bbc.AccessibilityViolationType;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.state.IScreenState;

/**
 * Created by marceloeler on 26/06/17.
 */

public class TargetSizeAccessibilityCheck implements IWidgetAccessibilityCheck {

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (!widget.isActionable())
            return null;

        float density = Registry.getContext().getResources().getDisplayMetrics().density;
        Rect bounds = new Rect();
        float height = Math.abs(widget.getY2() - widget.getY1());
        float width = Math.abs(widget.getX2() - widget.getX1());

        float relativeHeight = height/density;
        float relativeWidth = width/density;
        String sizeInfo = relativeHeight+"x"+relativeWidth;
        //MATE.log("Size info: " + sizeInfo + " of "+widget.getText() + " " + widget.getId() + " " + widget.getClazz());
        if (relativeHeight < AccessibilitySettings.MIN_HEIGHT || relativeWidth < AccessibilitySettings.MIN_WIDTH) {

            return new AccessibilityViolation(AccessibilityViolationType.SMALL_TOUCH_AREA, widget, state, sizeInfo);
        }
         return null;
    }

}
