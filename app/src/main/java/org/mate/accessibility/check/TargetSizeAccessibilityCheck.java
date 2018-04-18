package org.mate.accessibility.check;

import org.mate.accessibility.AccessibilitySettings;
import org.mate.ui.Widget;

/**
 * Created by marceloeler on 26/06/17.
 */

public class TargetSizeAccessibilityCheck implements IWidgetAccessibilityCheck{

    public int w=0;
    public int h=0;
    @Override
    public boolean check(Widget widget) {

        if (!widget.isExecutable())
            return true;

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
        return dimensionOK;
    }
}
