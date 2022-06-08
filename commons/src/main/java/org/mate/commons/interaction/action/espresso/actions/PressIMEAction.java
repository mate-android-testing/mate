package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.pressImeActionButton;

import android.os.Parcel;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.test.espresso.ViewAction;

import java.util.HashSet;
import java.util.Set;

public class PressIMEAction extends EspressoViewAction {
    public PressIMEAction() {
        super(EspressoViewActionType.PRESS_IME);
    }

    @Override
    public ViewAction getViewAction() {
        return pressImeActionButton();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        if (!hasIMEAction(view)) {
            return false;
        }

        return getViewAction().getConstraints().matches(view);
    }

    /**
     * Returns whether a given View has an IME action or not.
     */
    public boolean hasIMEAction(View view) {
        EditorInfo editorInfo = new EditorInfo();
        InputConnection inputConnection = view.onCreateInputConnection(editorInfo);
        if (inputConnection == null) {
            // View does not support input methods
            return false;
        }

        int actionId = editorInfo.actionId != 0
                ? editorInfo.actionId
                : editorInfo.imeOptions & EditorInfo.IME_MASK_ACTION;

        if (actionId == EditorInfo.IME_ACTION_NONE) {
            // No available action on view
            return false;
        }

        return true;
    }

    @Override
    public String getCode() {
        return "pressImeActionButton()";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.action.ViewActions.pressImeActionButton");
        return imports;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected PressIMEAction(Parcel in) {
        this();
    }

    public static final Creator<PressIMEAction> CREATOR = new Creator<PressIMEAction>() {
        @Override
        public PressIMEAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (PressIMEAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public PressIMEAction[] newArray(int size) {
            return new PressIMEAction[size];
        }
    };
}
