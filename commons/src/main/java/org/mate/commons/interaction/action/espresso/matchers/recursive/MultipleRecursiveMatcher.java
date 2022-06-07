package org.mate.commons.interaction.action.espresso.matchers.recursive;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;
import org.mate.commons.interaction.action.espresso.matchers.base.WithClassNameMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.WithContentDescriptionMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.WithIdMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.WithResourceNameMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.WithTextMatcher;
import org.mate.commons.interaction.action.espresso.matchers_combination.MatcherForPath;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTreeNode;
import org.mate.commons.interaction.action.espresso.view_tree.PathStep;
import org.mate.commons.interaction.action.espresso.view_tree.PathStepType;

import java.util.List;

public abstract class MultipleRecursiveMatcher extends EspressoViewMatcher {
    protected List<EspressoViewMatcher> matchers;

    public MultipleRecursiveMatcher(EspressoViewMatcherType type) {
        super(type);
    }

    public List<EspressoViewMatcher> getMatchers() {
        return matchers;
    }

    public void addMatcher(EspressoViewMatcher matcher) {
        matchers.add(matcher);
    }

    public void addMatcherForPathInTree(MatcherForPath matcherForPath,
                                        EspressoViewTreeNode node) {
        if (matcherForPath.getPath().isEmpty()) {
            EspressoViewMatcher newMatcher = null;

            switch (matcherForPath.getType()) {
                case WITH_RESOURCE_NAME:
                    newMatcher = new WithResourceNameMatcher(node.getEspressoView().getResourceName());
                    break;
                case WITH_ID:
                    newMatcher = new WithIdMatcher(node.getEspressoView().getId());
                    break;
                case WITH_TEXT:
                    newMatcher = new WithTextMatcher(node.getEspressoView().getText());
                    break;
                case WITH_CONTENT_DESCRIPTION:
                    newMatcher =
                            new WithContentDescriptionMatcher(node.getEspressoView().getContentDescription());
                    break;
                case WITH_CLASS_NAME:
                    newMatcher =
                            new WithClassNameMatcher(node.getEspressoView().getClassName());
                    break;
                default:
                    throw new IllegalStateException(String.format("Adding matcher in tree not " +
                            "implemented for matcher type: %s", matcherForPath.getType()));
            }

            addMatcher(newMatcher);

            return;
        }

        // If the path is not empty, it means that we need to keep traversing the nested
        // Matchers, while at the same time traversing the view tree.

        PathStep nextStepInPath = matcherForPath.getPath().getHead();
        EspressoViewTreeNode nodeAfterStep = nextStepInPath.moveFromNode(node);

        if (nextStepInPath.getType() == PathStepType.MOVE_TO_PARENT) {
            // Going "up" one level in the hierarchy tree
            // We need to use a WithParent matcher, but first we need to check that there isn't
            // one already
            WithParentMatcher withParentMatcher = null;
            for (EspressoViewMatcher matcher : matchers) {
                if (matcher instanceof WithParentMatcher) {
                    withParentMatcher = (WithParentMatcher) matcher;
                }
            }

            if (withParentMatcher == null) {
                withParentMatcher = new WithParentMatcher();
            }

            matchers.add(withParentMatcher);

            withParentMatcher.addMatcherForPathInTree(new MatcherForPath(
                    matcherForPath.getPath().getTail(),
                    matcherForPath.getType()), nodeAfterStep);
        } else {
            // Going "down" one level in the hierarchy tree
            // We need to use a WithChild matcher, but first we need to check that there isn't
            // one already
            WithChildMatcher withChildMatcher = null;
            for (EspressoViewMatcher matcher : matchers) {
                if (matcher instanceof WithChildMatcher) {
                    withChildMatcher = (WithChildMatcher) matcher;
                }
            }

            if (withChildMatcher == null) {
                withChildMatcher = new WithChildMatcher();
            }

            matchers.add(withChildMatcher);

            withChildMatcher.addMatcherForPathInTree(new MatcherForPath(
                    matcherForPath.getPath().getTail(),
                    matcherForPath.getType()), nodeAfterStep);
        }

    }
}
