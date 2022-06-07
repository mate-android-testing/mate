package org.mate.representation.interaction;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.input_generation.Mutation;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.EspressoAction;
import org.mate.representation.ExplorationInfo;

/**
 * ActionExecutor class for Espresso actions.
 */
public class EspressoActionExecutor extends ActionExecutor {

    public EspressoActionExecutor() {
        super();

        Mutation.setRandom(ExplorationInfo.getInstance().getRandom());
    }

    /**
     * Executes a given action.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    @Override
    public boolean perform(Action action) throws AUTCrashException {
        return executeAction((EspressoAction) action);
    }

    /**
     * Executes a widget action, e.g. a click on a certain widget.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    private boolean executeAction(EspressoAction action) throws AUTCrashException {
        return action.execute();
    }
}
