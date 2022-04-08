package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.doubleClick;

import androidx.test.espresso.ViewAction;

public class DoubleClickAction extends EspressoViewAction {
    public DoubleClickAction() {
        super(EspressoViewActionType.DOUBLE_CLICK);
    }

    @Override
    public ViewAction getViewAction() {
        return doubleClick();
    }

    @Override
    public String getCode() {
        return "doubleClick()";
    }
}
