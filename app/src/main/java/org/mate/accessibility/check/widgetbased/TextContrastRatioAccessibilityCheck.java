package org.mate.accessibility.check.widgetbased;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilitySettings;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.state.IScreenState;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

/**
 * Created by marceloeler on 26/06/17.
 */

public class TextContrastRatioAccessibilityCheck implements IWidgetAccessibilityCheck {


    private String packageName;
    private String stateId;
    private double contratio;
    boolean screenShot;

    public TextContrastRatioAccessibilityCheck(){
        contratio=21;
        screenShot=false;
    }

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {
        this.packageName = state.getPackageName();
        this.stateId = state.getId();

        if (!needsTextContrastChecked(widget))
                return null;
        contratio=21;
        double contrastRatio = Registry.getEnvironmentManager().getContrastRatio(packageName,stateId,widget);
        contratio=contrastRatio;
        MATE.log("Checked: " + widget.getClazz()+" txt:"+ widget.getText()+ " hint: " + widget.getHint()+":"+widget.getContentDesc()+" contrast ratio: " + contrastRatio);
        if (contrastRatio< AccessibilitySettings.MIN_CONTRAST_RATIO)
            return new AccessibilityViolation(AccessibilityViolationTypes.LOW_CONTRAST_RATIO,widget,state,String.valueOf(contrastRatio));
        return null;
    }

    public boolean needsTextContrastChecked(Widget widget) {

        if (!widget.isImportantForAccessibility())
            return false;

        if (widget.getBounds().equals("[0,0][0,0]"))
            return false;

        if (widget.getClazz().contains(("Image")))
            return false;

        if (widget.isEditable() && widget.getText().equals(""))
            return false;

        if (widget.mightBeImage())
            return false;

        if (widget.isActionable() && widget.getClazz().contains("Text") && widget.getText().equals(""))
            return false;


        return true;
    }


}
