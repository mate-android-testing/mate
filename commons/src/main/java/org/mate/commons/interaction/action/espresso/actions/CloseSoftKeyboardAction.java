package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;

import androidx.test.espresso.ViewAction;

public class CloseSoftKeyboardAction extends EspressoViewAction {
    public CloseSoftKeyboardAction() {
        super(EspressoViewActionType.CLOSE_SOFT_KEYBOARD);
    }

    @Override
    public ViewAction getViewAction() {
        return closeSoftKeyboard();
    }

    @Override
    public String getCode() {
        return "closeSoftKeyboard()";
    }
}
