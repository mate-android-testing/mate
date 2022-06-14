package org.mate.representation.state.espresso;

import android.content.res.Resources;
import android.view.View;

import org.mate.commons.interaction.action.espresso.EspressoView;
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
import org.mate.representation.input_generation.TextDataGenerator;

import java.util.ArrayList;
import java.util.List;

public class EspressoViewActionsParser {

    private final EspressoView espressoView;

    public EspressoViewActionsParser(EspressoView espressoView) {
        this.espressoView = espressoView;
    }

    public List<EspressoViewAction> parse() {
        List<EspressoViewAction> parsedActions = new ArrayList<>();

        if (espressoView.isAndroidView()) {
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
            new TypeTextAction("") // use empty text until we know we can use this action
        };

        for (EspressoViewAction action : possibleActions) {
            if (action.isValidForView(espressoView.getView())) {

                if (action instanceof TypeTextAction) {
                    // change empty text for a more interesting one
                    ((TypeTextAction) action).setText(TextDataGenerator.getInstance().
                            generateTextData(espressoView));
                }

                parsedActions.add(action);
            }
        }

        return parsedActions;
    }
}
