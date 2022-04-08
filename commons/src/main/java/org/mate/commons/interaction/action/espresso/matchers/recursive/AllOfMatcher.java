package org.mate.commons.interaction.action.espresso.matchers.recursive;

import static org.hamcrest.Matchers.allOf;

import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.List;

public class AllOfMatcher extends EspressoViewMatcher {
    private List<EspressoViewMatcher> matchers;

    public AllOfMatcher(List<EspressoViewMatcher> matchers) {
        super(EspressoViewMatcherType.ALL_OF);
        this.matchers = matchers;
    }

    @Override
    public String getCode() {
        StringBuilder viewMatchers = new StringBuilder();
        for (EspressoViewMatcher matcher : matchers) {
            viewMatchers.append(String.format("%s, ", matcher.getCode()));
        }

        // Delete last comma and space
        viewMatchers.deleteCharAt(viewMatchers.length() - 1);
        viewMatchers.deleteCharAt(viewMatchers.length() - 1);

        return String.format("allOf(%s)", viewMatchers.toString());
    }

    @Override
    public Matcher<View> getViewMatcher() {
        Matcher<View>[] viewMatchers = new Matcher[matchers.size()];
        for (int i = 0; i < matchers.size(); i++) {
            viewMatchers[i] = matchers.get(i).getViewMatcher();
        }

        return allOf(viewMatchers);
    }
}
