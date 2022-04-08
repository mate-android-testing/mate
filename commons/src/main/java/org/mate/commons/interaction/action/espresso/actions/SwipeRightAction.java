package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.swipeRight;

import androidx.test.espresso.ViewAction;

public class SwipeRightAction extends EspressoViewAction {
    public SwipeRightAction() {
        super(EspressoViewActionType.SWIPE_RIGHT);
    }

    @Override
    public ViewAction getViewAction() {
        return swipeRight();
    }

    @Override
    public String getCode() {
        return "swipeRight()";
    }
}
