package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.pressBackUnconditionally;

import androidx.test.espresso.ViewAction;

public class BackAction extends EspressoViewAction {
    public BackAction() {
        super(EspressoViewActionType.BACK);
    }

    @Override
    public ViewAction getViewAction() {
        return pressBackUnconditionally();
    }

    @Override
    public String getCode() {
        return "pressBackUnconditionally()";
    }
}
