package org.mate.interaction.action.ui;

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
    CLEAR_WIDGET,
    MANUAL_ACTION,
    TOGGLE_ROTATION,
    HOME,
    QUICK_SETTINGS,
    SEARCH,
    NOTIFICATIONS,
    SLEEP,
    WAKE_UP,
    DELETE,
    DPAP_UP,
    DPAD_DOWN,
    DPAD_LEFT,
    DPAD_RIGHT,
    DPAD_CENTER,
    ENTER;

    public static final ActionType[] primitiveActionTypes = {
            CLICK,
            LONG_CLICK,
            SWIPE_UP,
            SWIPE_DOWN,
            SWIPE_LEFT,
            SWIPE_RIGHT,
            BACK,
            MENU
    };
}
