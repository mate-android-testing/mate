package org.mate.representation.interaction;

import android.content.Context;

import androidx.test.uiautomator.UiDevice;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.Action;
import org.mate.representation.DeviceInfo;

public abstract class ActionExecutor {
    protected final UiDevice device;
    protected final Context targetContext;

    public ActionExecutor() {
        device = DeviceInfo.getInstance().getUiDevice();
        targetContext = DeviceInfo.getInstance().getAUTContext();
    }

    public abstract boolean perform(Action action) throws AUTCrashException;
}
