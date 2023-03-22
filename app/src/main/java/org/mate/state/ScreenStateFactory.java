package org.mate.state;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.interaction.DeviceMgr;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.intent.IntentAction;
import org.mate.interaction.action.intent.IntentBasedAction;
import org.mate.interaction.action.intent.SystemAction;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.state.executables.ActionsScreenState;
import org.mate.state.executables.AppScreen;

import java.util.List;

/**
 * A simple factory to retrieve the current screen state.
 */
public class ScreenStateFactory {

    /**
     * Tries to retrieve the current screen state.
     *
     * @param stateType The type of screen state that should be retrieved.
     * @return Returns the current screen state if possible.
     */
    public static IScreenState getScreenState(ScreenStateType stateType) {

        MATE.log_debug("Try retrieving screen state ...");

        // TODO: get rid of this static reference
        final DeviceMgr deviceMgr = Registry.getDeviceMgr();

        switch (stateType) {
            case ACTION_SCREEN_STATE:
                return new ActionsScreenState(new AppScreen(deviceMgr));
            default:
                throw new UnsupportedOperationException("State type " + stateType + "not yet supported!");
        }
    }


    public static IScreenState newDummyState() {
        return new IScreenState() {
            @Override
            public String getId() {
                return "DummyId";
            }

            @Override
            public void setId(String stateId) {
                throw new UnsupportedOperationException("Do not call this method!");
            }

            @Override
            public List<Widget> getWidgets() {
                throw new UnsupportedOperationException("Do not call this method!");
            }

            @Override
            public List<Action> getActions() {
                throw new UnsupportedOperationException("Do not call this method!");
            }

            @Override
            public List<UIAction> getUIActions() {
                throw new UnsupportedOperationException("Do not call this method!");
            }

            @Override
            public List<SystemAction> getSystemActions() {
                throw new UnsupportedOperationException("Do not call this method!");
            }

            @Override
            public List<IntentBasedAction> getIntentBasedActions() {
                throw new UnsupportedOperationException("Do not call this method!");
            }

            @Override
            public List<IntentAction> getIntentActions() {
                throw new UnsupportedOperationException("Do not call this method!");
            }

            @Override
            public List<WidgetAction> getWidgetActions() {
                throw new UnsupportedOperationException("Do not call this method!");
            }

            @Override
            public List<MotifAction> getMotifActions() {
                throw new UnsupportedOperationException("Do not call this method!");
            }

            @Override
            public String getActivityName() {
                return "DummyActivity";
            }

            @Override
            public String getPackageName() {
                return "DummyPackage";
            }

            @Override
            public ScreenStateType getType() {
                return ScreenStateType.DUMMY_SCREEN_STATE;
            }
        };
    }
}
