package org.mate;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.accessibility.ContrastRatioAccessibilityCheck;
import org.mate.interaction.DeviceMgr;
import org.mate.model.IGUIModel;
import org.mate.model.graph.GraphGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.AccessibilityChecker;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

import java.util.Date;
import java.util.Vector;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by marceloeler on 29/06/17.
 */
@RunWith(AndroidJUnit4.class)
public class CheckScreen {

    @Test
    public void useAppContext() throws Exception {
        IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
        Vector<Action> actions = screenState.getActions();

        MATE.log("Widgets: " );
        for (Widget w: screenState.getWidgets()){
            MATE.log(w.getId()+ " " + w.getClazz());
        }
        MATE.log("ACTIONS: " );
        for (Action action: actions){
            MATE.log(action.getActionType() + " - " + action.getWidget().getId() + " - " + action.getWidget().getText());
        }

        MATE.log("");
        MATE.log("");

        //iguiModel.updateModel(null,screenState);
        //screenState.setId(String.valueOf(time.getTime()));
        //EnvironmentManager.screenShot(screenState.getPackageName(),screenState.getId());
        //AccessibilityChecker.checkAccessibility(screenState.getPackageName(),iguiModel);

    }

}
