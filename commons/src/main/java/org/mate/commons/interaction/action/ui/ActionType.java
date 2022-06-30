package org.mate.commons.interaction.action.ui;

public enum ActionType {
    CLICK,
    LONG_CLICK,
    TYPE_TEXT,
    TYPE_SPECIFIC_TEXT,
    SWIPE_UP,
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT,
    BACK,
    MENU,
    CLEAR_TEXT,
    MANUAL_ACTION,
    TOGGLE_ROTATION,
    HOME,
    QUICK_SETTINGS,
    SEARCH,
    NOTIFICATIONS,
    SLEEP,
    WAKE_UP,
    DELETE,
    DPAD_UP,
    DPAD_DOWN,
    DPAD_LEFT,
    DPAD_RIGHT,
    DPAD_CENTER,
    ENTER,
    FILL_FORM_AND_SUBMIT,
    SPINNER_SCROLLING;

    public static final ActionType[] primitiveActionTypes = {
            CLICK,
            LONG_CLICK,
            SWIPE_UP,
            SWIPE_DOWN,
            SWIPE_LEFT,
            SWIPE_RIGHT,
            TYPE_TEXT,
            BACK,
            MENU
    };

    public static final ActionType[] motifActionTypes = {
            FILL_FORM_AND_SUBMIT,
            SPINNER_SCROLLING
    };
}
