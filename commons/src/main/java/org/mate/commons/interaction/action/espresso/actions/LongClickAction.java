package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.longClick;

import androidx.test.espresso.ViewAction;

public class LongClickAction extends EspressoViewAction {
    public LongClickAction() {
        super(EspressoViewActionType.LONG_CLICK);
    }

    @Override
    public ViewAction getViewAction() {
        return longClick();
    }

    @Override
    public String getCode() {
        return "longClick()";
    }
}
