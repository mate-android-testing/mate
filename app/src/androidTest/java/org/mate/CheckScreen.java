package org.mate;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.List;


/**
 * Created by marceloeler on 29/06/17.
 */
@RunWith(AndroidJUnit4.class)
public class CheckScreen {

    @Test
    public void useAppContext() throws Exception {

        IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
        List<WidgetAction> actions = screenState.getActions();

        MATE.log("Widgets: " );
        for (Widget w: screenState.getWidgets()){
            MATE.log(w.getId()+ " " + w.getClazz());
        }
        MATE.log("ACTIONS: " );
        for (WidgetAction action: actions){
            MATE.log(action.getActionType() + " - " + action.getWidget().getId() + " - " + action.getWidget().getText());
        }

        MATE.log("");
        MATE.log("");
        
    }

}
