package org.mate.commons.interaction.action.espresso.matchers_combination;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;
import org.mate.commons.interaction.action.espresso.view_tree.PathInTree;

public class MatcherForPath {

    private PathInTree pathFromTarget;
    private EspressoViewMatcherType type;

    public MatcherForPath(PathInTree pathFromTarget, EspressoViewMatcherType type) {
        this.pathFromTarget = pathFromTarget;
        this.type = type;
    }

    public PathInTree getPath() {
        return pathFromTarget;
    }

    public EspressoViewMatcherType getType() {
        return type;
    }
}
