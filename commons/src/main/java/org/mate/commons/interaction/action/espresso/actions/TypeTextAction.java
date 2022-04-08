package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.typeText;

import androidx.test.espresso.ViewAction;

public class TypeTextAction extends EspressoViewAction {
    private String stringToBeTyped;

    public TypeTextAction(String stringToBeTyped) {
        super(EspressoViewActionType.TYPE_TEXT);
        this.stringToBeTyped = stringToBeTyped;
    }

    @Override
    public ViewAction getViewAction() {
        if (!stringToBeTyped.endsWith("\n")) {
            // Appending a \n to the end of the string translates to a ENTER key event.
            stringToBeTyped += "\n";
        }

        return typeText(stringToBeTyped);
    }

    @Override
    public String getCode() {
        return String.format("typeText(%s)", boxString(stringToBeTyped));
    }
}
