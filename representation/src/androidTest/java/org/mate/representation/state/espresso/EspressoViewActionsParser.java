package org.mate.representation.state.espresso;

import android.content.res.Resources;
import android.view.View;

import org.mate.commons.interaction.action.espresso.actions.ClearTextAction;
import org.mate.commons.interaction.action.espresso.actions.ClickAction;
import org.mate.commons.interaction.action.espresso.actions.DoubleClickAction;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewAction;
import org.mate.commons.interaction.action.espresso.actions.LongClickAction;
import org.mate.commons.interaction.action.espresso.actions.PressIMEAction;
import org.mate.commons.interaction.action.espresso.actions.ScrollToAction;
import org.mate.commons.interaction.action.espresso.actions.SwipeDownAction;
import org.mate.commons.interaction.action.espresso.actions.SwipeLeftAction;
import org.mate.commons.interaction.action.espresso.actions.SwipeRightAction;
import org.mate.commons.interaction.action.espresso.actions.SwipeUpAction;
import org.mate.commons.interaction.action.espresso.actions.TypeTextAction;
import org.mate.commons.utils.MATELog;
import org.mate.representation.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

public class EspressoViewActionsParser {

    private final View view;

    public EspressoViewActionsParser(View view) {
        this.view = view;
    }

    public List<EspressoViewAction> parse() {
        List<EspressoViewAction> parsedActions = new ArrayList<>();

        if (isAndroidView()) {
            // we don't perform actions on Android views
            return parsedActions;
        }

        // There are many actions that we can possible perform on a View (e.g., Click, ClearText,
        // etc).
        // To determine if an action is valid for a View, we check that the later matches the
        // constraints imposed by the actual Espresso's ViewAction.
        EspressoViewAction[] possibleActions = {
            new ClearTextAction(),
            new ClickAction(),
            new DoubleClickAction(),
            new LongClickAction(),
            new PressIMEAction(),
            new ScrollToAction(),
            new SwipeDownAction(),
            new SwipeLeftAction(),
            new SwipeRightAction(),
            new SwipeUpAction(),
            // TODO (Ivan): Provide a properly generated text, not just a food
            new TypeTextAction("Hamburger")
        };

        for (EspressoViewAction action : possibleActions) {
            if (action.isValidForView(view)) {
                parsedActions.add(action);
            }
        }

        return parsedActions;
    }

    /**
     * Returns a boolean indicating whether the view being parsed is an Android view (e.g.,
     * created by the OS) or not.
     */
    public boolean isAndroidView() {
        String resourceName = getResourceName();
        if (resourceName == null) {
            return false;
        }

        return resourceName.startsWith("android")
                || resourceName.startsWith("com.google.android")
                || resourceName.startsWith("com.android");
    }

    public String getResourceName() {
        int id = view.getId();
        if (View.NO_ID == id) {
            return null;
        }

        try {
            return DeviceInfo.getInstance().getAUTContext().getResources().getResourceName(id);
        } catch (Resources.NotFoundException e) {
            MATELog.log_warn(String.format("Unable to find resource name for view with id %d", id));
            return null;
        }
    }
}
