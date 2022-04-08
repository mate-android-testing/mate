package org.mate.commons.interaction.action.espresso.matchers.recursive;

import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class HasDescendantMatcher extends EspressoViewMatcher {
    private EspressoViewMatcher matcher;

    public HasDescendantMatcher(EspressoViewMatcher matcher) {
        super(EspressoViewMatcherType.HAS_DESCENDANT);
        this.matcher = matcher;
    }

    @Override
    public String getCode() {
        return String.format("hasDescendant(%s)", matcher.getCode());
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return hasDescendant(matcher.getViewMatcher());
    }
}
