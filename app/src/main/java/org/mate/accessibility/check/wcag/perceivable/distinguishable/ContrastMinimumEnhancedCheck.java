package org.mate.accessibility.check.wcag.perceivable.distinguishable;

import org.mate.MATE;
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

        double contrastRatio = 21;
        if (widget.getContrast()==100 || widget.getContrast()==0) {
            contrastRatio = Registry.getEnvironmentManager().getContrastRatio(packageName, stateId, widget);
        }
        else
            contrastRatio = widget.getContrast();
        contratio=contrastRatio;
        //MATE.log("Checked: " + widget.getClazz()+" txt:"+ widget.getText()+ " hint: " + widget.getHint()+":"+widget.getContentDesc()+" contrast ratio: " + contrastRatio);
        //MATE.log(widget.getText()+ " - contrast: " + contrastRatio);
        if (contrastRatio<4.5) {
            //MATE.log("contrast issue");
            return new AccessibilityViolation(AccessibilityViolationType.CONSTRAST_MINUMUM, widget, state, String.valueOf(contrastRatio));
        }
        if (contrastRatio<7){
            //MATE.log("contrast issue enhanced");
            //return new AccessibilityViolation(AccessibilityViolationType.CONSTRAST_ENHANCED,widget,state,String.valueOf(contrastRatio));
        }
        return null;
    }

    public static boolean needsTextContrastChecked(Widget widget) {


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
