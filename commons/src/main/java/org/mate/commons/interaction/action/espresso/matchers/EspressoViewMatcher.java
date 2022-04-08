package org.mate.commons.interaction.action.espresso.matchers;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.EspressoCodeProducer;

public abstract class EspressoViewMatcher extends EspressoCodeProducer {
    private EspressoViewMatcherType type;

    public EspressoViewMatcher(EspressoViewMatcherType type) {
        this.type = type;
    }

    public EspressoViewMatcherType getType() {
        return type;
    }

    public abstract Matcher<View> getViewMatcher();
}
