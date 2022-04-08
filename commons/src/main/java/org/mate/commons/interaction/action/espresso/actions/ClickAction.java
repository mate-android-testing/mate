package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.click;

import androidx.test.espresso.ViewAction;

public class ClickAction extends EspressoViewAction {
    public ClickAction() {
        super(EspressoViewActionType.CLICK);
    }

    @Override
    public ViewAction getViewAction() {
        return click();
    }

    @Override
    public String getCode() {
        return "click()";
    }
}
