package org.mate.commons.interaction.action.espresso.matchers.recursive;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithParentMatcher extends EspressoViewMatcher {
    private EspressoViewMatcher matcher;

    public WithParentMatcher(EspressoViewMatcher matcher) {
        super(EspressoViewMatcherType.WITH_PARENT);
        this.matcher = matcher;
    }
}
