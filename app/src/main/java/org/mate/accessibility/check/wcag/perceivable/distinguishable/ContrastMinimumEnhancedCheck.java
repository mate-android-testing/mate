package org.mate.accessibility.check.wcag.perceivable.distinguishable;

import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class ContrastMinimumEnhancedCheck implements IWCAGCheck {

    private String packageName;
    private String stateId;
    private double contratio;
    boolean screenShot;

    public ContrastMinimumEnhancedCheck(){
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
        //MATE.log("Checked: " + widget.getClazz()+" txt:"+ widget.getText()+ " hint: " + widget.getHint()+":"+widget.getContentDesc()+" contrast ratio: " + contrastRatio);
        if (contrastRatio<4.5)
            return new AccessibilityViolation(AccessibilityViolationType.CONSTRAST_MINUMUM,widget,state,String.valueOf(contrastRatio));
        if (contrastRatio<7){
            return new AccessibilityViolation(AccessibilityViolationType.CONSTRAST_ENHANCED,widget,state,String.valueOf(contrastRatio));
        }
        return null;
    }

    public boolean needsTextContrastChecked(Widget widget) {


        //if (!widget.isImportantForAccessibility())
         // return false;

        if (widget.hasChildren())
            return false;

        if (widget.getText().equals(""))
            return false;

        if (widget.getClazz().contains(("Image")))
            return false;

        if (widget.mightBeImage())
            return false;

        if (widget.getClazz().contains("android.widget.Switch"))
            return false;

        if (widget.getClazz().contains("android.widget.ProgressBar"))
            return false;

        if (widget.getClazz().contains("Text") && widget.getText().equals(""))
            return false;

        if (widget.getClazz().contains("Toggle"))
            return false;

        if (widget.getClazz().contains("Check"))
            return false;

        if (widget.getClazz().contains("Radio"))
            return false;

        return true;
    }
}
