package org.mate.accessibility.check.bbc.screenbased;

import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class FlickeringAccessibilityCheck implements IWidgetAccessibilityCheck {

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        Registry.getEnvironmentManager().screenShotForFlickerDetection(state.getPackageName(),state.getId());


        return null;
    }
}
