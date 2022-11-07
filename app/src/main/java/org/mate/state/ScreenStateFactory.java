package org.mate.state;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.interaction.DeviceMgr;
import org.mate.state.executables.ActionsScreenState;
import org.mate.state.executables.AppScreen;

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
}
