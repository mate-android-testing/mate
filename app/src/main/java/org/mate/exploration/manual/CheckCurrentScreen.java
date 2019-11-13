package org.mate.exploration.manual;

import org.mate.MATE;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.accessibility.check.ContrastRatioAccessibilityCheck;
import org.mate.accessibility.check.FormControlLabelCheck;
import org.mate.accessibility.check.MultipleContentDescCheck;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.state.IScreenState;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

import static org.mate.MATE.device;

public class CheckCurrentScreen {

    public void scanScreen(){

        IScreenState screenState = MATE.uiAbstractionLayer.getLastScreenState();
        EnvironmentManager.screenShot(screenState.getPackageName(),screenState.getId());

        MATE.log("Current screen state: " + screenState.getId());

        //MATE.log("Widgets: " );
        for (Widget w: screenState.getWidgets()){
            MATE.log(w.getId()+ " " + w.getClazz()+ " " + w.getText() + " " + w.isVisibleToUser() + " " + w.isHeading());
            MATE.log("labeled by: " + w.getLabeledBy() + "  label for: " + w.getLabelFor() + " hint: " + w.getHint());
            MATE.log("\n");
            MATE.log("");
        }

        MATE.log("");


        //MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(screenState);
        //ContrastRatioAccessibilityCheck contrastChecker = new ContrastRatioAccessibilityCheck(screenState.getPackageName(),screenState.getActivityName(),screenState.getId(),device
          //      .getDisplayWidth(),device.getDisplayHeight());
        FormControlLabelCheck formCheck = new FormControlLabelCheck(screenState.getWidgets());
        for (Widget widget: screenState.getWidgets()) {

            //boolean contrastRatioOK = contrastChecker.check(widget);
            //MATE.log("Check contrast of "+widget.getId() + ": " + contrastChecker.contratio);

            //if (!contrastRatioOK)
              //  AccessibilitySummaryResults.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",widget,String.valueOf(contrastChecker.contratio));

            //boolean multDescOK = multDescChecker.check(widget);
            //if (!multDescOK)
                //AccessibilitySummaryResults.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW",widget,"");

            boolean formLabelOK = formCheck.check(widget);
            if (!formLabelOK) {
                MATE.log("FORM CONTROL LABEL ERROR: " + " - " + widget.getClazz() + " - " + widget.getId() + " - " + widget.getText());
            }

        }


    }
}
