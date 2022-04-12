package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;

import android.os.Parcel;

import androidx.test.espresso.ViewAction;

public class CloseSoftKeyboardAction extends EspressoViewAction {
    public CloseSoftKeyboardAction() {
        super(EspressoViewActionType.CLOSE_SOFT_KEYBOARD);
    }

    @Override
    public ViewAction getViewAction() {
        return closeSoftKeyboard();
    }

    @Override
    public String getCode() {
        return "closeSoftKeyboard()";
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
