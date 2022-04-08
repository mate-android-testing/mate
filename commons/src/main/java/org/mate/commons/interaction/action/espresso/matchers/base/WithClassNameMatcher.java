package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static org.hamcrest.Matchers.equalTo;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithClassNameMatcher extends EspressoViewMatcher {
    private String className;

    public WithClassNameMatcher(String className) {
        super(EspressoViewMatcherType.WITH_CLASS_NAME);
        this.className = className;
    }

    @Override
    public String getCode() {
        return String.format("withClassName(equalTo(%s))", boxString(className));
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withClassName(equalTo(className));
    }
}
