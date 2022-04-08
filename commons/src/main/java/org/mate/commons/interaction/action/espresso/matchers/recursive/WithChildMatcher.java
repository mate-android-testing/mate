package org.mate.commons.interaction.action.espresso.matchers.recursive;

import static androidx.test.espresso.matcher.ViewMatchers.withChild;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithChildMatcher extends EspressoViewMatcher {
    private EspressoViewMatcher matcher;

    public WithChildMatcher(EspressoViewMatcher matcher) {
        super(EspressoViewMatcherType.WITH_CHILD);
        this.matcher = matcher;
    }

    @Override
    public String getCode() {
        return String.format("withChild(%s)", matcher.getCode());
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withChild(matcher.getViewMatcher());
    }
}
