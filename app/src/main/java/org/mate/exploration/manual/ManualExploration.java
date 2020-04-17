package org.mate.exploration.manual;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.IAccessibilityViolationChecker;
import org.mate.accessibility.check.bbc.AccessibilityViolationCheckerBBC;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.ActionType;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.Date;
import java.util.List;

/**
 * Created by geyan on 11/06/2017.
 */

public class ManualExploration {


    public ManualExploration(){

    }

    public void startManualExploration(long runningTime) {

        IAccessibilityViolationChecker bbcChecker = new AccessibilityViolationCheckerBBC();

        long currentTime = new Date().getTime();

        MATE.log("MATE TIMEOUT: " + MATE.TIME_OUT);
        Action manualAction = new WidgetAction(ActionType.MANUAL_ACTION);
        while (currentTime - runningTime <= MATE.TIME_OUT){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");

            for (Widget w: state.getWidgets()){
                MATE.log_acc(w.getClazz()+"-"+w.getId()+"-"+w.getText()+"-"+w.getBounds());
            }


            boolean foundNewState  = MATE.uiAbstractionLayer.checkIfNewState(state);
            if (foundNewState){

                MATE.uiAbstractionLayer.executeAction(manualAction);
                state = MATE.uiAbstractionLayer.getLastScreenState();
                Registry.getEnvironmentManager().screenShot(state.getPackageName(),state.getId());


                MATE.logactivity(state.getActivityName());


                AccessibilitySummaryResults.currentActivityName=state.getActivityName();
                AccessibilitySummaryResults.currentPackageName=state.getPackageName();

                List<AccessibilityViolation> violations = bbcChecker.runAccessibilityChecks(state);

                MATE.log_acc("Amount of violations found: " + violations.size());
                for (AccessibilityViolation violation: violations){
                    MATE.log_acc(violation.getType() + " " + violation.getWidget().getId() + ":"+violation.getWidget().getText()+ " -- " + violation.getInfo());
                }

                //MATE.log_acc("CHECK CONTRAST");

                /*
                MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck();
                TextContrastRatioAccessibilityCheck contrastChecker = new TextContrastRatioAccessibilityCheck();
                for (Widget widget: state.getWidgets()) {

                    AccessibilityViolation contrastRatioViolationFound = contrastChecker.check(state, widget);
                    //MATE.log("Check contrast of "+widget.getId() + ": " + contrastChecker.contratio);

                    if (contrastRatioViolationFound!=null)
                        AccessibilitySummaryResults.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",widget,contrastRatioViolationFound.getInfo());

                    AccessibilityViolation multDescViolationFound = multDescChecker.check(state, widget);
                    if (multDescViolationFound!=null)
                        AccessibilitySummaryResults.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW",widget,"");

                }*/

            }
            currentTime = new Date().getTime();
        }
    }

}
