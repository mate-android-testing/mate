package org.mate.commons.interaction.action.espresso.matchers;

import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTreeNode;

public enum EspressoViewMatcherType {
    // Base matchers
    IS_ROOT,
    WITH_CLASS_NAME,
    WITH_CONTENT_DESCRIPTION,
    WITH_ID,
    WITH_TEXT,
    WITH_HINT,
    WITH_RESOURCE_NAME,
    // Recursive matchers
    ALL_OF,
    ANY_OF,
    HAS_DESCENDANT,
    IS_DESCENDANT_OF_A,
    WITH_CHILD,
    WITH_PARENT,
    ;

    public boolean isValidForEspressoViewTreeNode(EspressoViewTreeNode node) {
        if (this == WITH_TEXT) {
            return node.getEspressoView().getText() != null;
        } else if (this == WITH_CONTENT_DESCRIPTION) {
            return node.getEspressoView().getContentDescription() != null;
        } else if (this == WITH_RESOURCE_NAME) {
            return node.getEspressoView().getResourceName() != null;
        } else if (this == HAS_DESCENDANT || this == WITH_CHILD) {
            return node.getChildren().size() > 0;
        } else if (this == IS_DESCENDANT_OF_A || this == WITH_PARENT) {
            return node.getParent() != null;
        }

        return true;
    }
}
