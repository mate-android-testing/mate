package org.mate.commons.interaction.action.espresso.matchers.base;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithHintMatcher extends EspressoViewMatcher {
    private String hint;

    public WithHintMatcher(String hint) {
        super(EspressoViewMatcherType.WITH_HINT);
        this.hint = hint;
    }
}
