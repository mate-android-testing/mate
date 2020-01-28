package org.mate.accessibility.check.widgetbased;

import android.content.Context;
import android.graphics.Rect;


import org.mate.accessibility.AccessibilitySettings;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationType;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by marceloeler on 26/06/17.
 */

public class TargetSizeAccessibilityCheck implements IWidgetAccessibilityCheck {

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (!widget.isActionable())
            return null;

        Context generalContext = getInstrumentation().getContext();
        float density = generalContext.getResources().getDisplayMetrics().density;
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
