package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static org.hamcrest.Matchers.equalTo;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithContentDescriptionMatcher extends EspressoViewMatcher {
    private String contentDescription;

    public WithContentDescriptionMatcher(String contentDescription) {
        super(EspressoViewMatcherType.WITH_CONTENT_DESCRIPTION);
        this.contentDescription = contentDescription;
    }

    @Override
    public String getCode() {
        return String.format("withContentDescription(equalTo(%s))", boxString(contentDescription));
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withContentDescription(equalTo(contentDescription));
    }
}
