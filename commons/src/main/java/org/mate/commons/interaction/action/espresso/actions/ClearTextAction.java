package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.clearText;

import androidx.test.espresso.ViewAction;

public class ClearTextAction extends EspressoViewAction {
    public ClearTextAction() {
        super(EspressoViewActionType.CLEAR_TEXT);
    }

    @Override
    public ViewAction getViewAction() {
        return clearText();
    }

    @Override
    public String getCode() {
        return "clearText()";
    }
}
