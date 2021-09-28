package org.mate.state;

import org.mate.MATE;
import org.mate.state.executables.ActionsScreenState;
import org.mate.state.executables.AppScreen;
import org.mate.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple factory to retrieve the current screen state.
 */
public class ScreenStateFactory {

    static int numberEntries = 0;
    public static List<Long> intermediateValues = new ArrayList<>();
    static Long average = null;

    /**
     * Indicates how many retrials are allowed when fetching
     * the current screen state.
     */
    private static final int MAX_NUMBER_OF_RETRIES = 3;

    /**
     * Tries to retrieve the current screen state.
     *
     * @param stateType The type of screen state that should be retrieved.
     * @return Returns the current screen state if possible.
     */
    public static IScreenState getScreenState(ScreenStateType stateType) {
        long startTime = System.currentTimeMillis();
        intermediateValues.add(startTime); //1
        MATE.log_debug("Try retrieving screen state ...");

        switch (stateType) {
            case ACTION_SCREEN_STATE:

                IScreenState state =  new ActionsScreenState(new AppScreen());
                int retries = 0;
                /*
                 * TODO: Verify whether retrieving a screen state can fail at all.
                 *    In addition, how to distinguish between a faulty screen state
                 *    and a screen state with zero actions?
                 */
                while (retries < MAX_NUMBER_OF_RETRIES && state.getActions().size() == 0) {
                    MATE.log_debug("Retry fetching screen state!");
                    Utils.sleep(5000);
                    state = new ActionsScreenState(new AppScreen());
                    retries++;
                }
                if (state.getActions().size() == 0) {
                    MATE.log_warn("Fetching screen state failed!");
                }
                evaluateTime(startTime);
                return state;
            default:
                evaluateTime(startTime);
                throw new UnsupportedOperationException("State type " + stateType + "not yet supported!");
        }
    }

    static void evaluateTime(long startTime) {
        long currentTime = System.currentTimeMillis();
        StringBuilder stb = new StringBuilder("Intermediates: ");
        for (Long intermediate : intermediateValues) {
            intermediate = intermediate - intermediateValues.get(0);

            stb.append(intermediate);
            stb.append("ms ");

        }
        stb.append("Duration: ");
        stb.append(currentTime - startTime).append("ms");
        numberEntries++;
        if (average == null) {
            average = currentTime - startTime;
        } else {
            average = ((numberEntries - 1) * average + (currentTime - startTime)) / numberEntries;
        }
        stb.append(" Average: ").append(average).append("ms");
        intermediateValues.clear();
        MATE.log_runtime(stb.toString(), "getScreenState()");
    }
}
