package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.swipeDown;

import androidx.test.espresso.ViewAction;

public class SwipeDownAction extends EspressoViewAction {
    public SwipeDownAction() {
        super(EspressoViewActionType.SWIPE_DOWN);
    }

    @Override
    public ViewAction getViewAction() {
        return swipeDown();
    }

    @Override
    public String getCode() {
        return "swipeDown()";
    }
}
