package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.swipeUp;

import androidx.test.espresso.ViewAction;

public class SwipeUpAction extends EspressoViewAction {
    public SwipeUpAction() {
        super(EspressoViewActionType.SWIPE_UP);
    }

    @Override
    public ViewAction getViewAction() {
        return swipeUp();
    }

    @Override
    public String getCode() {
        return "swipeUp()";
    }
}
