package org.mate.commons.interaction.action.espresso.matchers.base;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class IsRootMatcher extends EspressoViewMatcher {

    public IsRootMatcher() {
        super(EspressoViewMatcherType.IS_ROOT);
    }
}
