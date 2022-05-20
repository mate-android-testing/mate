package org.mate.state;

import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.interaction.DeviceMgr;
import org.mate.state.executables.ActionsScreenState;
import org.mate.state.executables.AppScreen;
import org.mate.commons.utils.Utils;

/**
 * A simple factory to retrieve the current screen state.
 */
public class ScreenStateFactory {

    /**
     * Indicates how many retrials are allowed when fetching the current screen state.
     */
    private static final int MAX_NUMBER_OF_RETRIES = 3;

    /**
     * Tries to retrieve the current screen state.
     *
     * @param stateType The type of screen state that should be retrieved.
     * @return Returns the current screen state if possible.
     */
    public static IScreenState getScreenState(ScreenStateType stateType) {

        MATELog.log_debug("Try retrieving screen state ...");

        // TODO: get rid of this static reference
        final DeviceMgr deviceMgr = Registry.getDeviceMgr();

        switch (stateType) {
            case ACTION_SCREEN_STATE:

                IScreenState state =  new ActionsScreenState(new AppScreen(deviceMgr));
                int retries = 0;

                /*
                 * TODO: Verify whether retrieving a screen state can fail at all.
                 *    In addition, how to distinguish between a faulty screen state
                 *    and a screen state with zero actions?
                 */
                while (retries < MAX_NUMBER_OF_RETRIES && state.getActions().size() == 0) {
                    MATELog.log_debug("Retry fetching screen state!");
                    Utils.sleep(5000);
                    state = new ActionsScreenState(new AppScreen(deviceMgr));
                    retries++;
                }

                if (state.getActions().size() == 0) {
                    MATELog.log_warn("Fetching screen state failed!");
                }

                return state;
            default:
                throw new UnsupportedOperationException("State type " + stateType + "not yet supported!");
        }
    }
}
