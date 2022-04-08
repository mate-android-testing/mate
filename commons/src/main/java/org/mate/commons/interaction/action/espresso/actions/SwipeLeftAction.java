package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.swipeLeft;

import androidx.test.espresso.ViewAction;

public class SwipeLeftAction extends EspressoViewAction {
    public SwipeLeftAction() {
        super(EspressoViewActionType.SWIPE_LEFT);
    }

    @Override
    public ViewAction getViewAction() {
        return swipeLeft();
    }

    @Override
    public String getCode() {
        return "swipeLeft()";
    }
}
