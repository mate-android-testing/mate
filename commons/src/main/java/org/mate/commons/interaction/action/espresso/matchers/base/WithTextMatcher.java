package org.mate.commons.interaction.action.espresso.matchers.base;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithTextMatcher extends EspressoViewMatcher {
    private String text;

    public WithTextMatcher(String text) {
        super(EspressoViewMatcherType.WITH_TEXT);
        this.text = text;
    }
}
