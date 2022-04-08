package org.mate.commons.interaction.action.espresso.matchers.base;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithIdMatcher extends EspressoViewMatcher {
    private int id;

    public WithIdMatcher(int id) {
        super(EspressoViewMatcherType.WITH_ID);
        this.id = id;
    }
}
