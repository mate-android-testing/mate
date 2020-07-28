package org.mate.ui;

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
    ENTER,
    RESTART,
    STARTAPP;

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
