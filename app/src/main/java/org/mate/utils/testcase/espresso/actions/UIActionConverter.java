package org.mate.utils.testcase.espresso.actions;

import android.view.KeyEvent;

import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.UIAction;

import java.util.EnumSet;

import static org.mate.utils.testcase.espresso.EspressoDependency.IS_ROOT;
import static org.mate.utils.testcase.espresso.EspressoDependency.KEY_EVENT;
import static org.mate.utils.testcase.espresso.EspressoDependency.PRESS_BACK;
import static org.mate.utils.testcase.espresso.EspressoDependency.PRESS_KEY;

/**
 * Converts a {@link UIAction} to an espresso action.
 */
public class UIActionConverter extends ActionConverter {

    /**
     * The set of {@link UIAction}s that can't be natively translated to espresso actions.
     */
    private static final EnumSet<ActionType> NON_TRANSLATABLE_ACTIONS = EnumSet.of(ActionType.HOME,
            ActionType.BACK, ActionType.TOGGLE_ROTATION, ActionType.MENU, ActionType.SEARCH);

    /**
     * The action type of the {@link UIAction}.
     */
    private final ActionType actionType;

    /**
     * Constructs a converter for the given {@link UIAction}.
     *
     * @param action The ui action that should be converted.
     */
    public UIActionConverter(UIAction action) {
        super(action);
        this.actionType = action.getActionType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String convert() {
        // certain ui actions can't be translated to native espresso actions
        if (NON_TRANSLATABLE_ACTIONS.contains(actionType)) {
            // we fall back to the help of the UiAutomator
            buildPerform();
            buildComment();
            return builder.toString();
        } else {
            return super.convert();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildComment() {
        super.buildComment();
        builder.append(actionType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildPerform() {
        switch (actionType) {
            case HOME:
                builder.append("device.pressHome();");
                break;
            case BACK:
                builder.append(PRESS_BACK).append("();");
                break;
            case MENU:
                builder.append("device.pressMenu();");
                break;
            case TOGGLE_ROTATION:
                builder.append("rotate();");
                break;
            case SEARCH:
                builder.append("device.pressSearch();");
                break;
            case ENTER:
                builder.append(".perform(")
                        .append(PRESS_KEY)
                        .append("(")
                        .append(KEY_EVENT).append(".")
                        .append(KeyEvent.keyCodeToString(KeyEvent.KEYCODE_ENTER))
                        .append("));");
                break;
            case DELETE:
                builder.append(".perform(")
                        .append(PRESS_KEY)
                        .append("(")
                        .append(KEY_EVENT).append(".")
                        .append(KeyEvent.keyCodeToString(KeyEvent.KEYCODE_DEL))
                        .append("));");
                break;
            default:
                throw new UnsupportedOperationException("Action type " + actionType + " not yet supported!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildViewMatcher() {
        builder.append(IS_ROOT).append("()");
    }
}
