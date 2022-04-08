package org.mate.commons.interaction.action.espresso.matchers.recursive;

import static androidx.test.espresso.matcher.ViewMatchers.withParent;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithParentMatcher extends EspressoViewMatcher {
    private EspressoViewMatcher matcher;

    public WithParentMatcher(EspressoViewMatcher matcher) {
        super(EspressoViewMatcherType.WITH_PARENT);
        this.matcher = matcher;
    }

    @Override
    public String getCode() {
        return String.format("withParent(%s)", matcher.getCode());
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withParent(matcher.getViewMatcher());
    }
}
