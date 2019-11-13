package org.mate.exploration.manual;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityInfoChecker;
import org.mate.accessibility.check.widgetbased.ContrastRatioAccessibilityCheck;
import org.mate.accessibility.check.screenbased.MultipleContentDescCheck;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.ActionType;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.Date;

import static org.mate.MATE.device;

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

            IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");

            for (Widget w: state.getWidgets()){
                MATE.log_acc(w.getClazz()+"-"+w.getId()+"-"+w.getText()+"-"+w.getBounds());
            }


            boolean foundNewState  = MATE.uiAbstractionLayer.checkIfNewState(state);
            if (foundNewState){

                MATE.uiAbstractionLayer.executeAction(manualAction);
                state = MATE.uiAbstractionLayer.getLastScreenState();
                EnvironmentManager.screenShot(state.getPackageName(),state.getId());


                MATE.logactivity(state.getActivityName());

                AccessibilityInfoChecker accChecker = new AccessibilityInfoChecker();
                AccessibilitySummaryResults.currentActivityName=state.getActivityName();
                AccessibilitySummaryResults.currentPackageName=state.getPackageName();
                accChecker.runAccessibilityTests(state);
                //MATE.log_acc("CHECK CONTRAST");

                MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(state);
                ContrastRatioAccessibilityCheck contrastChecker = new ContrastRatioAccessibilityCheck();
                for (Widget widget: state.getWidgets()) {

                    boolean contrastRatioViolationFound = contrastChecker.check(state, widget);
                    //MATE.log("Check contrast of "+widget.getId() + ": " + contrastChecker.contratio);

                    if (contrastRatioViolationFound)
                        AccessibilitySummaryResults.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",widget,contrastChecker.getInfo());

                    boolean multDescViolationFound = multDescChecker.check(state, widget);
                    if (multDescViolationFound)
                        AccessibilitySummaryResults.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW",widget,"");

                }

            }
            currentTime = new Date().getTime();
        }
    }

}
