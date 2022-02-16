package org.mate.representation.interaction;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Intent;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.intent.ComponentType;
import org.mate.commons.interaction.action.intent.IntentBasedAction;
import org.mate.commons.utils.MATELog;

public class IntentBasedActionExecutor extends ActionExecutor {

    /**
     * Executes a given action.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    public boolean perform(Action action) throws AUTCrashException {
        return executeAction((IntentBasedAction) action);
    }

    /**
     * Executes an Intent-based action. Depending on the target component, either
     * startActivity(), startService() or sendBroadcast() is invoked.
     *
     * @param action The action which contains the Intent to be sent.
     */
    private boolean executeAction(IntentBasedAction action) throws AUTCrashException {

        Intent intent = action.getIntent();

        try {
            switch (action.getComponentType()) {
                case ACTIVITY:
                    targetContext.startActivity(intent);
                    break;
                case SERVICE:
                    targetContext.startService(intent);
                    break;
                case BROADCAST_RECEIVER:
                    targetContext.sendBroadcast(intent);
                    break;
                default:
                    throw new UnsupportedOperationException("Component type not supported yet!");
            }

            return true;
        } catch (Exception e) {
            final String msg = "Calling startActivity() from outside of an Activity  context " +
                    "requires the FLAG_ACTIVITY_NEW_TASK flag.";
            if (e.getMessage().contains(msg) && action.getComponentType() == ComponentType.ACTIVITY) {
                MATELog.log("Retrying sending intent with ACTIVITY_NEW_TASK flag!");
                try {
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    targetContext.startActivity(intent);

                    return true;
                } catch (Exception ex) {
                    MATELog.log("Executing Intent-based action failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                MATELog.log("Executing Intent-based action failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return false;
    }
}
