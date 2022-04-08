package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithIdMatcher extends EspressoViewMatcher {
    private int id;

    public WithIdMatcher(int id) {
        super(EspressoViewMatcherType.WITH_ID);
        this.id = id;
    }

    @Override
    public String getCode() {
        return String.format("withId(%d)", id);
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withId(id);
    }
}
