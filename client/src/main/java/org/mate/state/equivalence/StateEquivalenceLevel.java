package org.mate.state.equivalence;

/**
 * Defines the various supported abstractions used in the screen states equality comparison.
 */
public enum StateEquivalenceLevel {

    /**
     * Two screen states are equal if they share the same package name. This is the weakest
     * abstraction level and implies that all screen states of the app are treated as one model state.
     */
    PACKAGE_NAME,

    /**
     * Two screen states are equal if they refer to the same package and activity. This means that
     * each activity defines an own model state.
     */
    ACTIVITY_NAME,

    /**
     * Two screen states are equal if they refer to the same package and activity and contain the
     * same number of widgets and the widgets are at the same position. This is the default
     * abstraction level.
     */
    WIDGET,

    /**
     * Two screen states are equal if they refer to the same package and activity and contain the
     * same widgets, where the content description and the text within a text field of a widget are
     * considered as well.
     */
    WIDGET_WITH_ATTRIBUTES,

    /**
     * Two screen states are equal if the cosine similarity coefficient is above the specified
     * cosine similarity threshold.
     */
    COSINE_SIMILARITY;
}
