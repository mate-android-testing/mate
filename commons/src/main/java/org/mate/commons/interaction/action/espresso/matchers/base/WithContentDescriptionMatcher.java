package org.mate.commons.interaction.action.espresso.matchers.base;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithContentDescriptionMatcher extends EspressoViewMatcher {
    private String contentDescription;

    public WithContentDescriptionMatcher(String contentDescription) {
        super(EspressoViewMatcherType.WITH_CONTENT_DESCRIPTION);
        this.contentDescription = contentDescription;
    }
}
