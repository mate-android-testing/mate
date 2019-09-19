package org.mate.interaction;

import org.mate.exceptions.AUTCrashException;
import org.mate.model.IGUIModel;
import org.mate.ui.WidgetAction;

/**
 * Created by marceloeler on 22/06/17.
 */

public interface IApp {

    public void executeAction(WidgetAction action) throws AUTCrashException;
    public void reinstallApp();
    public void restartApp();
    public void handleCrashDialog();
    public boolean goToState(IGUIModel guiModel, String targetScreenStateId);
    public void sleep(long time);
}
