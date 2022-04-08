package org.mate.commons.interaction.action.espresso.matchers;

public enum EspressoViewMatcherType {
    // Base matchers
    WITH_CLASS_NAME,
    WITH_CONTENT_DESCRIPTION,
    WITH_ID,
    WITH_TEXT_OR_HINT,
    // Intermediate matchers
    ALL_OF,
    ANY_OF,
    HAS_DESCENDANT,
    IS_IMMEDIATE_DESCENDANT,
}
