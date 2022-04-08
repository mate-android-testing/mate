package org.mate.commons.interaction.action.espresso.matchers.recursive;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithChildMatcher extends EspressoViewMatcher {
    private EspressoViewMatcher matcher;

    public WithChildMatcher(EspressoViewMatcher matcher) {
        super(EspressoViewMatcherType.WITH_CHILD);
        this.matcher = matcher;
    }
}
