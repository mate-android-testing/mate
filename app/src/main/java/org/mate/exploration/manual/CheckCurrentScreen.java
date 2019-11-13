package org.mate.exploration.manual;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolationChecker;
import org.mate.accessibility.check.widgetbased.FormControlLabelCheck;
import org.mate.state.IScreenState;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

public class CheckCurrentScreen {

    public void scanScreen(){

        IScreenState screenState = MATE.uiAbstractionLayer.getLastScreenState();
        EnvironmentManager.screenShot(screenState.getPackageName(),screenState.getId());

        MATE.log("Current screen state: " + screenState.getId());

        //MATE.log("Widgets: " );
        for (Widget w: screenState.getWidgets()){
            MATE.log(w.getId()+ " " + w.getClazz()+ " " + w.getText() + " " + w.isVisibleToUser() + " " + w.isHeading());
            if (w.isEditable())
                MATE.log("INPUT TYPE: " + w.getInputType());
            MATE.log("\n");
            MATE.log("");
        }

        MATE.log("");

        AccessibilityViolationChecker.runAccessibilityChecks(screenState);


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


    }
}
