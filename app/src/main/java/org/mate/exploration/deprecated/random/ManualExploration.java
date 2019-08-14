package org.mate.exploration.deprecated.random;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityInfoChecker;
import org.mate.accessibility.check.ContrastRatioAccessibilityCheck;
import org.mate.accessibility.check.MultipleContentDescCheck;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.interaction.DeviceMgr;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.ActionType;
import org.mate.ui.Widget;

import java.util.Date;
import java.util.List;

import static org.mate.MATE.device;

/**
 * Created by geyan on 11/06/2017.
 */

@Deprecated
public class ManualExploration {
    private DeviceMgr deviceMgr;
    private String packageName;
    private MATE mate;
    private IScreenState launchState;
    private List<Action> executableActions;
    private IGUIModel guiModel;

    public ManualExploration(DeviceMgr deviceMgr,
                             String packageName, MATE mate, IGUIModel guiModel){
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        this.mate = mate;
        this.guiModel = guiModel;
    }

    public Action getActionSplash(List<Action> actions){
        Action action = null;
        for (Action act: actions){
            if (act.getWidget().getClazz().contains("ImageButton")) {
                if (act.getWidget().getId().contains("next") ||  act.getWidget().getId().contains("done"))
                    return act;
            }
        }
        return action;
    }

    public void startManualExploration(long runningTime) {

        long currentTime = new Date().getTime();

        MATE.log("MATE TIMEOUT: " + MATE.TIME_OUT);
        int cont=  0;
        while (currentTime - runningTime <= MATE.TIME_OUT){
            cont++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");

            String currentPackageName = state.getPackageName();
            //MATE.log("package name: " + currentPackageName);
            if (currentPackageName==null) {
                 MATE.logsum("CURRENT PACKAGE: NULL");
                 return;
            }

            boolean newState = guiModel.updateModel(new Action(new Widget("","",""), ActionType.CLICK),state);
            if (newState || cont==1){
                MATE.logactivity(state.getActivityName());
                AccessibilityInfoChecker accChecker = new AccessibilityInfoChecker();
                AccessibilitySummaryResults.currentActivityName=state.getActivityName();
                AccessibilitySummaryResults.currentPackageName=state.getPackageName();
                accChecker.runAccessibilityTests(state);
                //MATE.log_acc("CHECK CONTRAST");
                MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(state);
                ContrastRatioAccessibilityCheck contrastChecker = new ContrastRatioAccessibilityCheck(state.getPackageName(),state.getActivityName(),state.getId(),device
                        .getDisplayWidth(),device.getDisplayHeight());
                for (Widget widget: state.getWidgets()) {

                    boolean contrastRatioOK = contrastChecker.check(widget);
                    //MATE.log("Check contrast of "+widget.getId() + ": " + contrastChecker.contratio);

                    if (!contrastRatioOK)
                        AccessibilitySummaryResults.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",widget,String.valueOf(contrastChecker.contratio));

                    boolean multDescOK = multDescChecker.check(widget);
                    if (!multDescOK)
                        AccessibilitySummaryResults.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW",widget,"");

                }
            }

            currentTime = new Date().getTime();
        }
    }

}
