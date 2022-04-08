package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.pressKey;

import android.view.KeyEvent;

import androidx.test.espresso.ViewAction;

public class EnterAction extends EspressoViewAction {
    public EnterAction() {
        super(EspressoViewActionType.ENTER);
    }

    @Override
    public ViewAction getViewAction() {
        return pressKey(KeyEvent.KEYCODE_ENTER);
    }

    @Override
    public String getCode() {
        return "pressKey(KeyEvent.KEYCODE_ENTER)";
    }
}
