package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.scrollTo;

import androidx.test.espresso.ViewAction;

public class ScrollToAction extends EspressoViewAction {
    public ScrollToAction() {
        super(EspressoViewActionType.SCROLL_TO);
    }

    @Override
    public ViewAction getViewAction() {
        return scrollTo();
    }

    @Override
    public String getCode() {
        return "scrollTo()";
    }
}
