package org.mate.commons.interaction.action.espresso.matchers_combination;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.recursive.AllOfMatcher;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTree;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTreeNode;

import java.util.List;

public class EspressoViewMatcherBuilder {

    private final EspressoViewTreeNode targetNode;
    private final EspressoViewTree viewTree;

    AllOfMatcher viewMatcher = new AllOfMatcher();

    public EspressoViewMatcherBuilder(EspressoViewTreeNode targetNode, EspressoViewTree viewTree) {
        this.targetNode = targetNode;
        this.viewTree = viewTree;
    }

    public static EspressoViewMatcherBuilder addMatcherCombination(EspressoViewMatcherCombination matcherCombination) {
        EspressoViewMatcherBuilder builder = new EspressoViewMatcherBuilder(matcherCombination.getTargetNode(), matcherCombination.getViewTree());
        List<MatcherForPath> matchers = matcherCombination.getMatchers();

        for (MatcherForPath matcher : matchers) {
            builder.viewMatcher.addMatcherForPathInTree(matcher, matcherCombination.getTargetNode());
        }

        return builder;
    }

    public EspressoViewMatcher build() {
        return viewMatcher;
    }
}
