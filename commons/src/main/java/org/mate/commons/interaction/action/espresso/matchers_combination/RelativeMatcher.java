package org.mate.commons.interaction.action.espresso.matchers_combination;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;
import org.mate.commons.interaction.action.espresso.view_tree.PathInTree;

/**
 * Represents a matcher condition imposed on a relative view.
 * A relative view is a view that can be reached after traversing a path in the UI hierarchy tree,
 * starting from a target view.
 */
public class RelativeMatcher {

    /**
     * The path to traverse from target view to reach the relative view.
     */
    private final PathInTree pathFromTarget;

    /**
     * The type of matcher condition imposed in the relative view.
     */
    private final EspressoViewMatcherType type;

    public RelativeMatcher(PathInTree pathFromTarget, EspressoViewMatcherType type) {
        this.pathFromTarget = pathFromTarget;
        this.type = type;
    }

    /**
     * @return The path to traverse from target view to reach the relative view.
     */
    public PathInTree getPath() {
        return pathFromTarget;
    }

    /**
     * @return The type of matcher condition imposed in the relative view.
     */
    public EspressoViewMatcherType getType() {
        return type;
    }
}
