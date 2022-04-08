package org.mate.commons.interaction.action.espresso.matchers.recursive;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.List;

public class AllOfMatcher extends EspressoViewMatcher {
    private List<EspressoViewMatcher> matchers;

    public AllOfMatcher(List<EspressoViewMatcher> matchers) {
        super(EspressoViewMatcherType.ALL_OF);
        this.matchers = matchers;
    }
}
