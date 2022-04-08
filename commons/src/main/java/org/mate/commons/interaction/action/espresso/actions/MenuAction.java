package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.pressMenuKey;

import androidx.test.espresso.ViewAction;

public class MenuAction extends EspressoViewAction {
    public MenuAction() {
        super(EspressoViewActionType.MENU);
    }

    @Override
    public ViewAction getViewAction() {
        return pressMenuKey();
    }

    @Override
    public String getCode() {
        return "pressMenuKey()";
    }
}
