package org.mate.commons.interaction.action.espresso.matchers.recursive;

import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class IsDescendantOfAMatcher extends EspressoViewMatcher {
    private EspressoViewMatcher matcher;

    public IsDescendantOfAMatcher(EspressoViewMatcher matcher) {
        super(EspressoViewMatcherType.IS_DESCENDANT_OF_A);
        this.matcher = matcher;
    }

    @Override
    public String getCode() {
        return String.format("isDescendantOfA(%s)", matcher.getCode());
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return isDescendantOfA(matcher.getViewMatcher());
    }
}
