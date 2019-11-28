package org.mate.exploration.manual;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolationChecker;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class CheckCurrentScreen {

    public void scanScreen(){

        IScreenState screenState = MATE.uiAbstractionLayer.getLastScreenState();

        EnvironmentManager.screenShot(screenState.getPackageName(),screenState.getId());

        MATE.log("Current screen state: " + screenState.getId());

        for (Widget w: screenState.getWidgets()){
            MATE.log(w.getId()+ " " + w.getClazz()+ " text: " + w.getText() + "  IFA: " + w.isImportantForAccessibility() + "  AFOC: " + w.isAccessibilityFocused() + " actionable: " + w.isActionable() + " icc: " + w.isContextClickable() + " hint: " + w.getHint() + "  "+w.getContentDesc() + " visible: " + w.isVisibleToUser()+ " selected: " + w.isSelected() + " - " + w.isChecked());
            //if (w.getParent()!=null)
                //MATE.log("------ son of " + w.getParent().getClazz());
            //if (w.isEditable())
                //MATE.log("INPUT TYPE: " + w.getInputType());
            MATE.log("\n");
            MATE.log("");
        }

        AccessibilityViolationChecker.runAccessibilityChecks(screenState);

        try {
            MATE.log("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            MATE.log("WAIT WAIT WAIT");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
        AccessibilityViolationChecker.runAccessibilityChecks(screenState);



        //ContentResizingAccessibilityCheck contentResizingAccessibilityCheck = new ContentResizingAccessibilityCheck();
        //contentResizingAccessibilityCheck.check(screenState);


        //MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(screenState);
        //ContrastRatioAccessibilityCheck contrastChecker = new ContrastRatioAccessibilityCheck(screenState.getPackageName(),screenState.getActivityName(),screenState.getId(),device
          //      .getDisplayWidth(),device.getDisplayHeight());
        /*
        FormControlLabelCheck formCheck = new FormControlLabelCheck();
        for (Widget widget: screenState.getWidgets()) {

            //boolean contrastRatioOK = contrastChecker.check(widget);
            //MATE.log("Check contrast of "+widget.getId() + ": " + contrastChecker.contratio);

            //if (!contrastRatioOK)
              //  AccessibilitySummaryResults.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",widget,String.valueOf(contrastChecker.contratio));

            //boolean multDescOK = multDescChecker.check(widget);
            //if (!multDescOK)
                //AccessibilitySummaryResults.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW",widget,"");

            boolean formLabelOK = formCheck.check(screenState, widget);
            if (!formLabelOK) {
                MATE.log("FORM CONTROL LABEL ERROR: " + " - " + widget.getClazz() + " - " + widget.getId() + " - " + widget.getText());
            }

        }*/

        MATE.log("END OF CURRENT SCREEN VALIDATION");
    }

}
