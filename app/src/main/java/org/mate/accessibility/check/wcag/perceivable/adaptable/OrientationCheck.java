package org.mate.accessibility.check.wcag.perceivable.adaptable;

//https://www.w3.org/TR/WCAG21/#orientation
//https://www.w3.org/WAI/WCAG21/Understanding/orientation.html

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.state.executables.ActionsScreenState;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.ArrayList;
import java.util.List;

public class OrientationCheck implements IWCAGCheck {

    private IScreenState rotatedScreen;

    public OrientationCheck(){

    }


    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        try{
            MATE.device.unfreezeRotation();

            int before = MATE.device.getDisplayRotation();
            MATE.device.setOrientationRight();
            MATE.device.unfreezeRotation();
            //MATE.log ("before: " + before);
            int after = MATE.device.getDisplayRotation();
            //MATE.log ("after: " + after);
            MATE.device.setOrientationLeft();
            MATE.device.unfreezeRotation();
            Thread.sleep(200);
            if (before == after)
                return new AccessibilityViolation(AccessibilityViolationType.ORIENTATION, state,"");


        }
        catch(Exception e) {
            return new AccessibilityViolation(AccessibilityViolationType.ORIENTATION, state,"");
        }

        return null;
    }
}
