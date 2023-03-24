package org.mate.exploration.rl.qlearning.qbe.qmatrix;


import org.mate.interaction.action.ui.UIAction;

/**
 * Implementation according to
 * Y. Koroglu et al., "QBE: QLearning-Based Exploration of Android Applications,"
 * 2018 IEEE 11th International Conference on Software Testing, Verification and Validation (ICST),
 * 2018, pp. 105-115, doi: 10.1109/ICST.2018.00020.
 */
public final class QBEAbstractAction implements QMatrix.AbstractActions {
    @Override
    public int getAbstractActionIndex(final UIAction action) {
        switch (action.getActionType()) {
            case MENU:
                return 0;
            case BACK:
                return 1;
            case CLICK:
                return 2;
            case LONG_CLICK:
                return 3;
            case TYPE_TEXT:
            case TYPE_SPECIFIC_TEXT:
                return 4;
            case SWIPE_UP:
            case SWIPE_DOWN:
            case SWIPE_LEFT:
            case SWIPE_RIGHT:
            case DPAD_UP:
            case DPAD_DOWN:
            case DPAD_LEFT:
            case DPAD_RIGHT:
            case DPAD_CENTER:
                return 5;
            case CLEAR_TEXT:
            case MANUAL_ACTION:
            case TOGGLE_ROTATION:
            case HOME:
            case QUICK_SETTINGS:
            case SEARCH:
            case NOTIFICATIONS:
            case SLEEP:
            case WAKE_UP:
            case DELETE:
            case ENTER:
                return 6;
            default:
                throw new AssertionError("Unreachable!");
        }
    }

    @Override
    public int getNumberOfAbstractActions() {
        return 7;
    }
}
