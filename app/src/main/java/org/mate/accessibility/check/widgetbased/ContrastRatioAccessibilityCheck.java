package org.mate.accessibility.check.widgetbased;

import org.mate.accessibility.AccessibilitySettings;
import org.mate.state.IScreenState;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

/**
 * Created by marceloeler on 26/06/17.
 */

public class ContrastRatioAccessibilityCheck implements IWidgetAccessibilityCheck {


    private String packageName;
    private String stateId;
    private double contratio;
    boolean screenShot;

    public ContrastRatioAccessibilityCheck(){
        contratio=21;
        screenShot=false;
    }

    @Override
    public boolean check(IScreenState state, Widget widget) {
        this.packageName = state.getPackageName();
        this.stateId = state.getId();

        if (!widget.needsContrastChecked())
                return false;
        contratio=21;
        double contrastRatio = EnvironmentManager.getContrastRatio(packageName,stateId,widget);
        contratio=contrastRatio;
        if (contrastRatio< AccessibilitySettings.MIN_CONTRAST_RATIO)
            return true;
        return false;
    }

    @Override
    public String getType() {
        return "CONTRAST RATIO";
    }

    @Override
    public String getInfo() {
        return String.valueOf(contratio);
    }
}
