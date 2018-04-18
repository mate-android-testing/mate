package org.mate.accessibility.check;

import org.mate.accessibility.AccessibilitySettings;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

/**
 * Created by marceloeler on 26/06/17.
 */

public class ContrastRatioAccessibilityCheck implements IWidgetAccessibilityCheck{


    private String packageName;
    private String stateId;
    public double contratio;
    private String activity;
    int maxh;
    int maxw;
    boolean screenShot;

    public ContrastRatioAccessibilityCheck(String packageName, String activity, String stateId, int maxw, int maxh){
        contratio=21;
        this.packageName = packageName;
        this.stateId = stateId;
        this.maxw = maxw;
        this.maxh = maxh;
        this.activity=activity;
        screenShot=false;
    }

    @Override
    public boolean check(Widget widget) {
        if (!widget.needsContrastChecked())
                return true;
        contratio=21;
        double contrastRatio = EnvironmentManager.getContrastRatio(packageName,stateId,widget,maxw,maxh);
        contratio=contrastRatio;
        if (contrastRatio>= AccessibilitySettings.MIN_CONTRAST_RATIO)
            return true;
        return false;
    }
}
