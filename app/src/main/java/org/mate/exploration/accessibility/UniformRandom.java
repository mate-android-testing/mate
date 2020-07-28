package org.mate.exploration.accessibility;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.IAccessibilityViolationChecker;
import org.mate.accessibility.check.bbc.AccessibilityViolationCheckerBBC;
import org.mate.accessibility.check.bbc.widgetbased.MultipleContentDescCheck;
import org.mate.accessibility.check.bbc.widgetbased.TextContrastRatioAccessibilityCheck;
import org.mate.exceptions.AUTCrashException;
import org.mate.interaction.DeviceMgr;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.mate.MATE.device;
import static org.mate.MATE.uiAbstractionLayer;

public class UniformRandom extends AbstractRandomExploration {

    public UniformRandom(UIAbstractionLayer uiAbstractionLayer){
        super(uiAbstractionLayer);
    }

    @Override
    public WidgetAction nextAction(IScreenState state) {
        List<WidgetAction>  executableActions = state.getActions();
        Random rand = new Random();
        int index =  rand.nextInt(executableActions.size());
        WidgetAction action = executableActions.get(index);
        return action;
    }
/*

    private void runAccessibilityChecks(IScreenState state, IScreenState selectedScreenState) {


        Registry.getEnvironmentManager().screenShot(state.getPackageName(),state.getId());

        //updates the current activity name
        String currentActivityName = state.getActivityName();
        MATE.log("start ACCESSIBILITY CHECKS: " );
        MATE.logactivity(state.getActivityName());


        //prepare for collecting results
        AccessibilitySummaryResults.currentActivityName = state.getActivityName();
        AccessibilitySummaryResults.currentPackageName = state.getPackageName();

        //run accessibility checks implemented by Google ATF / eyes free:
        //   EditableContentDesc
        //   SpeakableTextPresent
        //   ClickableSpan
        //   TouchTargetSize
        //   DuplicateClickableBounds
        //bbcChecker.runAccessibilityChecks(state);

        //run accessibility checks implemented by MATE team

        //create checker for multiple (duplicate) content description
        MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck();
        //create checker for contrast issues
        TextContrastRatioAccessibilityCheck contrastChecker =
                new TextContrastRatioAccessibilityCheck();

        //run checks for each widget on the screen
        for (Widget widget: state.getWidgets()) {

            //run constrast check
            AccessibilityViolation contrastRatioViolationFound = contrastChecker.check(state, widget);

            if (contrastRatioViolationFound!=null) {
                //report accessibility flaw found
                MATE.log("ADD CONTRAST FLAW");
                AccessibilitySummaryResults.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",
                        widget, contrastRatioViolationFound.getInfo());



               // EnvironmentManager.markScreenshot(widget, selectedScreenState.getPackageName(),
                 //       selectedScreenState.getId(), "ACCESSIBILITY_CONTRAST_FLAW",
                    //    String.valueOf(contrastChecker.contratio));
            }

            //run multiple desc check
            AccessibilityViolation multViolation = multDescChecker.check(state,widget);
            if (multViolation!=null) {
                //report accessibility flaw found
                AccessibilitySummaryResults.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW", widget, "");

                //EnvironmentManager.markScreenshot(widget, selectedScreenState.getPackageName(),
                  //      selectedScreenState.getId(), "DUPLICATE_SPEAKABLE_TEXT_FLAW",
                    //    "");
            }
        }
        MATE.log("finish ACCESSIBILITY CHECKS: " );
    }




/*
    private long waitForProgressBar(IScreenState state) {
        long ini = new Date().getTime();
        long end = new Date().getTime();
        boolean hadProgressBar = false;
        boolean hasProgressBar = true;
        while (hasProgressBar && (end-ini)<22000) {
            hasProgressBar=false;
            for (Widget widget : state.getWidgets()) {
                if (widget.getClazz().contains("ProgressBar") && widget.isEnabled() && widget.getContentDesc().contains("Loading")) {
                    MATE.log("WAITING PROGRESS BAR TO FINISH");
                    hasProgressBar = true;
                    hadProgressBar=true;
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    state = ScreenStateFactory.getScreenState(state.getType());
                }
            }
            end = new Date().getTime();
        }
        if (!hadProgressBar)
            return 0;
        return end-ini;
    }
*/

}
