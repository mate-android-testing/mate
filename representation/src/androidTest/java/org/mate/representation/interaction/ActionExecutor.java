package org.mate.representation.interaction;

import android.content.Context;

import androidx.test.uiautomator.UiDevice;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.Action;
import org.mate.representation.DeviceInfo;

/**
 * Abstract class representing the base class of an Action executor.
 * Basically, an Action executor needs to implement a method to perform an action on request.
 */
public abstract class ActionExecutor {
    /**
     * The UiDevice provided by the DeviceInfo class.
     */
    protected final UiDevice device;

    /**
     * The AUT's Context provided by the DeviceInfo class.
     */
    protected final Context targetContext;

    public ActionExecutor() {
        device = DeviceInfo.getInstance().getUiDevice();
        targetContext = DeviceInfo.getInstance().getAUTContext();
    }

    /**
     * Execute the provided action.
     *
     * @param action
     * @return
     * @throws AUTCrashException
     */
    public abstract boolean perform(Action action) throws AUTCrashException;
}
