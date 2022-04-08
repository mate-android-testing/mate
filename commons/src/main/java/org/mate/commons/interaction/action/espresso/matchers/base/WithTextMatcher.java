package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithTextMatcher extends EspressoViewMatcher {
    private String text;

    public WithTextMatcher(String text) {
        super(EspressoViewMatcherType.WITH_TEXT);
        this.text = text;
    }

    @Override
    public String getCode() {
        return String.format("withText(equalTo(%s))", text);
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withText(equalTo(text));
    }
}
