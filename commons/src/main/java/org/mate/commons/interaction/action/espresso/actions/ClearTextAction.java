package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.clearText;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a Clear text Espresso action.
 */
public class ClearTextAction extends EspressoViewAction {
    public ClearTextAction() {
        super(EspressoViewActionType.CLEAR_TEXT);
    }

    @Override
    public ViewAction getViewAction() {
        return clearText();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        return getViewAction().getConstraints().matches(view);
    }

    @Override
    public String getCode() {
        return "clearText()";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.action.ViewActions.clearText");
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

    protected ClearTextAction(Parcel in) {
        this();
    }

    public static final Creator<ClearTextAction> CREATOR = new Creator<ClearTextAction>() {
        @Override
        public ClearTextAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (ClearTextAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public ClearTextAction[] newArray(int size) {
            return new ClearTextAction[size];
        }
    };
}
