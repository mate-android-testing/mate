package org.mate.commons.interaction.action.espresso.matchers;

public class EspressoViewMatcher {
    private EspressoViewMatcherType type;

    public EspressoViewMatcher(EspressoViewMatcherType type) {
        this.type = type;
    }

    public EspressoViewMatcherType getType() {
        return type;
    }
}
