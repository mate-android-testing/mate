package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static org.hamcrest.Matchers.equalTo;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithHintMatcher extends EspressoViewMatcher {
    private String hint;

    public WithHintMatcher(String hint) {
        super(EspressoViewMatcherType.WITH_HINT);
        this.hint = hint;
    }

    @Override
    public String getCode() {
        return String.format("withHint(equalTo(%s))", hint);
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withHint(equalTo(hint));
    }
}
