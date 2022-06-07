package org.mate.commons.interaction.action.espresso.matchers_combination;

import org.mate.commons.interaction.action.espresso.view_tree.PathInTree;

public class MatcherForPath {

    private PathInTree pathFromTarget;
    private BaseMatcherType type;

    public MatcherForPath(PathInTree pathFromTarget, BaseMatcherType type) {
        this.pathFromTarget = pathFromTarget;
        this.type = type;
    }

    public PathInTree getPath() {
        return pathFromTarget;
    }

    public BaseMatcherType getType() {
        return type;
    }
}
