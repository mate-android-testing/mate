package org.mate.state.executables;

import org.mate.Properties;

/**
 * Defines the various supported abstractions used in the {@link org.mate.state.IScreenState}
 * equality comparison.
 */
public enum StateEquivalenceLevel {

    /**
     * Two {@link org.mate.state.IScreenState}s are equal if they share the same package name. This
     * is the weakest abstraction level and implies that all screen states of the app are treated
     * as one model state.
     */
    PACKAGE_NAME,

    /**
     * Two {@link org.mate.state.IScreenState}s are equal if they refer to the same package and
     * activity. This means that each activity defines an own model state.
     */
    ACTIVITY_NAME,

    /**
     * Two {@link org.mate.state.IScreenState}s are equal if they refer to the same package and
     * activity and contain the same number of widgets and the widgets are at the same position.
     * This is the default abstraction level.
     */
    WIDGET,

    /**
     * Two {@link org.mate.state.IScreenState}s are equal if they refer to the same package and
     * activity and contain the same widgets, where the content description and the text within a
     * text field of a widget are considered as well.
     */
    WIDGET_WITH_ATTRIBUTES,

    /**
     * Two {@link org.mate.state.IScreenState}s are equal if they have a cosine similarity above
     * {@link Properties#COSINE_SIMILARITY_THRESHOLD()}.
     */
    COSINE_SIMILARITY;
}
