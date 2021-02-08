package org.mate.exploration.manual;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationChecker;
import org.mate.accessibility.check.bbc.widgetbased.TextContrastRatioAccessibilityCheck;
import org.mate.accessibility.check.bbc.widgetbased.MultipleContentDescCheck;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.state.ScreenStateType;

import java.util.Date;

/**
 * Created by geyan on 11/06/2017.
 */

public class ManualExploration {


    public ManualExploration(){

    }

    public void startManualExploration(long runningTime) {

        long currentTime = new Date().getTime();

        MATE.log("MATE TIMEOUT: " + MATE.TIME_OUT);
        Action manualAction = new WidgetAction(ActionType.MANUAL_ACTION);
        while (currentTime - runningTime <= MATE.TIME_OUT){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            IScreenState state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

            for (Widget w: state.getWidgets()){
                MATE.log_acc(w.getClazz()+"-"+w.getId()+"-"+w.getText()+"-"+w.getBounds());
            }


            boolean foundNewState  = MATE.uiAbstractionLayer.checkIfNewState(state);
            if (foundNewState){

                MATE.uiAbstractionLayer.executeAction(manualAction);
                state = MATE.uiAbstractionLayer.getLastScreenState();
                Registry.getEnvironmentManager().takeScreenshot(state.getPackageName(),state.getId());


                MATE.logactivity(state.getActivityName());


                AccessibilitySummaryResults.currentActivityName=state.getActivityName();
                AccessibilitySummaryResults.currentPackageName=state.getPackageName();
                AccessibilityViolationChecker.runAccessibilityChecks(state);
                //MATE.log_acc("CHECK CONTRAST");

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

                }

            }
            currentTime = new Date().getTime();
        }
    }

}
