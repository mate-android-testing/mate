package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

import java.util.HashSet;
import java.util.Set;

public class CloseSoftKeyboardAction extends EspressoViewAction {
    public CloseSoftKeyboardAction() {
        super(EspressoViewActionType.CLOSE_SOFT_KEYBOARD);
    }

    @Override
    public ViewAction getViewAction() {
        return closeSoftKeyboard();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        // This action can only be performed on the root view.
        return isRoot().matches(view);
    }

    @Override
    public String getCode() {
        return "closeSoftKeyboard()";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.action.ViewActions.closeSoftKeyboard");
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

    protected CloseSoftKeyboardAction(Parcel in) {
        this();
    }

    public static final Creator<CloseSoftKeyboardAction> CREATOR = new Creator<CloseSoftKeyboardAction>() {
        @Override
        public CloseSoftKeyboardAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (CloseSoftKeyboardAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public CloseSoftKeyboardAction[] newArray(int size) {
            return new CloseSoftKeyboardAction[size];
        }
    };
}
