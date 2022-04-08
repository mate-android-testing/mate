package org.mate.commons.interaction.action.espresso.matchers.recursive;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.List;

public class AnyOfMatcher extends EspressoViewMatcher {
    private List<EspressoViewMatcher> matchers;

    public AnyOfMatcher(List<EspressoViewMatcher> matchers) {
        super(EspressoViewMatcherType.ANY_OF);
        this.matchers = matchers;
    }
}
