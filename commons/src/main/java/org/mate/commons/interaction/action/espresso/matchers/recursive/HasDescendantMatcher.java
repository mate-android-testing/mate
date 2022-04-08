package org.mate.commons.interaction.action.espresso.matchers.recursive;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class HasDescendantMatcher extends EspressoViewMatcher {
    private EspressoViewMatcher matcher;

    public HasDescendantMatcher(EspressoViewMatcher matcher) {
        super(EspressoViewMatcherType.HAS_DESCENDANT);
        this.matcher = matcher;
    }
}
