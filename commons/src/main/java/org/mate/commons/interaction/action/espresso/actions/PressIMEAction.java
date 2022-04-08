package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.pressImeActionButton;

import androidx.test.espresso.ViewAction;

public class PressIMEAction extends EspressoViewAction {
    public PressIMEAction() {
        super(EspressoViewActionType.PRESS_IME);
    }

    @Override
    public ViewAction getViewAction() {
        return pressImeActionButton();
    }

    @Override
    public String getCode() {
        return "pressImeActionButton()";
    }
}
