package org.mate.accessibility.check.wcag.perceivable.distinguishable;

import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class NonTextContrastCheck implements IWCAGCheck {

    private String packageName;
    private String stateId;
    private double contratio;
    boolean screenShot;

    public NonTextContrastCheck(){
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
        if (contrastRatio<3)
            return new AccessibilityViolation(AccessibilityViolationType.NON_TEXT_CONTRAST,widget,state,String.valueOf(contrastRatio));

        return null;
    }

    public static boolean needsTextContrastChecked(Widget widget) {

        //if (!widget.isImportantForAccessibility())
          //  return false;


        if (widget.hasChildren())
            return false;

        if (widget.getClazz().contains(("Image")))
            return true;

        if (widget.getClazz().contains("Switch"))
            return true;

        if (widget.getClazz().contains("android.widget.ProgressBar"))
            return true;

        if (widget.mightBeImage())
            return true;

        if (widget.getClazz().contains("ImageButton")){
            return true;
        }

        if (widget.getClazz().contains("Toggle"))
            return true;

        if (widget.getClazz().contains("Check"))
            return true;

        if (widget.getClazz().contains("Radio"))
            return true;

        if (widget.isEditable() && widget.getText().equals(""))
            return true;

        if (!widget.getText().equals(""))
            return false;

        return true;
    }
}
