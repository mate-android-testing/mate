package org.mate.accessibility.check.wcag.operable.inputmodalities;

import android.content.Context;
import android.graphics.Rect;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.bbc.AccessibilitySettings;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class TargetSizeCheck implements IWCAGCheck {

    public boolean needsSizeChecked(Widget widget) {

        if (widget.hasChildren())
            return false;

        if (!widget.isActionable())
            return false;

        return true;
    }

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (!needsSizeChecked(widget))
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

            return new AccessibilityViolation(AccessibilityViolationType.TARGET_SIZE, widget, state, sizeInfo);
        }
        return null;
    }

}
