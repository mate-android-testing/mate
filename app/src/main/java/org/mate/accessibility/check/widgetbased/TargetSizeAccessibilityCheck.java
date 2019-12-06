package org.mate.accessibility.check.widgetbased;

import android.content.Context;
import android.graphics.Rect;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityInfoCheckResult;

import org.mate.accessibility.AccessibilitySettings;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.Locale;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by marceloeler on 26/06/17.
 */

public class TargetSizeAccessibilityCheck implements IWidgetAccessibilityCheck {

    public int w=0;
    public int h=0;
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

        if (relativeHeight < AccessibilitySettings.MIN_HEIGHT || relativeWidth < AccessibilitySettings.MIN_WIDTH) {
            String sizeInfo = relativeHeight+"x"+relativeWidth;
            return new AccessibilityViolation(AccessibilityViolationTypes.SMALL_TOUCH_AREA, widget, state, sizeInfo);
        }
        /*
        w=0;
        h=0;
        float dpiRatio = (float) AccessibilitySettings.DENSITY / 160;
        boolean dimensionOK=true;
        if (AccessibilitySettings.DENSITY!=0){
            int width = widget.getX2() - widget.getX1();
            int height = widget.getY1() - widget.getY2();
            int targetHeight = (int) (Math.abs(height) / dpiRatio);
            int targetWidth = (int) (Math.abs(width) / dpiRatio);
            h=targetHeight;
            w=targetWidth;

            if (targetHeight < AccessibilitySettings.MIN_HEIGHT || targetWidth < AccessibilitySettings.MIN_WIDTH)
                dimensionOK = false;
        }
        return null;
        */
         return null;
    }

}
