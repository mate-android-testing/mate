package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class IsRootMatcher extends EspressoViewMatcher {

    public IsRootMatcher() {
        super(EspressoViewMatcherType.IS_ROOT);
    }

    @Override
    public String getCode() {
        return "isRoot()";
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return isRoot();
    }
}
