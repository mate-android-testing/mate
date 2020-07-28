package org.mate.exploration.accessibility;

import android.support.test.InstrumentationRegistry;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.IAccessibilityViolationChecker;
import org.mate.accessibility.check.bbc.AccessibilityViolationCheckerBBC;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.accessibility.check.wcag.AccessibilityViolationCheckerWCAG;
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

    public void startManualExploration() {


        //default
        MATE.TIME_OUT = 2000;

        IAccessibilityViolationChecker wcagChecker = new AccessibilityViolationCheckerWCAG();
        long runningTime = new Date().getTime();
        long currentTime = new Date().getTime();

        long newTimeout = 0;
        String timeoutStr = InstrumentationRegistry.getArguments().getString("timeout");
        try{
            newTimeout = Long.valueOf(timeoutStr);
            MATE.TIME_OUT = newTimeout; //to minutes
        }
        catch(Exception e){
            MATE.log("Invalid timeout argument");

        }

        //MATE.log("MATE TIMEOUT: " + MATE.TIME_OUT + " ms");
        MATE.log("MATE TIMEOUT: " + MATE.TIME_OUT/1000 + " seg");

        Action manualAction = new WidgetAction(ActionType.MANUAL_ACTION);
        while (currentTime - runningTime <= MATE.TIME_OUT){

            IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");



            boolean foundNewState  = MATE.uiAbstractionLayer.checkIfNewState(state);
            if (foundNewState){
                MATE.log("START CHECKING NEW SCREEN");
                MATE.uiAbstractionLayer.executeAction(manualAction);
                state = MATE.uiAbstractionLayer.getLastScreenState();

                Registry.getEnvironmentManager().screenShot(state.getPackageName(),state.getId());

                MATE.logactivity(state.getActivityName());
                MATE.log("Visited activity: " + state.getActivityName());

                List<AccessibilityViolation> violations = wcagChecker.runAccessibilityChecks(state);

                MATE.log_acc("Amount of violations found: " + violations.size());
                for (AccessibilityViolation violation: violations){
                    MATE.log_acc(violation.getType() + " " + violation.getWidget().getId() + ":"+violation.getWidget().getText()+ " -- " + violation.getInfo());
                }
                MATE.log("NEW SCREEN CHECKED");
            }
            currentTime = new Date().getTime();


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
