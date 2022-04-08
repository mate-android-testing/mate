package org.mate.commons.interaction.action.espresso.matchers.recursive;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class IsDescendantOfAMatcher extends EspressoViewMatcher {
    private EspressoViewMatcher matcher;

    public IsDescendantOfAMatcher(EspressoViewMatcher matcher) {
        super(EspressoViewMatcherType.IS_DESCENDANT_OF_A);
        this.matcher = matcher;
    }
}
