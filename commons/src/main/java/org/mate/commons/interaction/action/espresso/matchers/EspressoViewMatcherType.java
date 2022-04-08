package org.mate.commons.interaction.action.espresso.matchers;

public enum EspressoViewMatcherType {
    // Base matchers
    IS_ROOT,
    WITH_CLASS_NAME,
    WITH_CONTENT_DESCRIPTION,
    WITH_ID,
    WITH_TEXT,
    WITH_HINT,
    // Recursive matchers
    ALL_OF,
    ANY_OF,
    HAS_DESCENDANT,
    IS_DESCENDANT_OF_A,
    WITH_CHILD,
    WITH_PARENT,
}
