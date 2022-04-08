package org.mate.commons.interaction.action.espresso.matchers.base;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithClassNameMatcher extends EspressoViewMatcher {
    private String className;

    public WithClassNameMatcher(String className) {
        super(EspressoViewMatcherType.WITH_CLASS_NAME);
        this.className = className;
    }
}
