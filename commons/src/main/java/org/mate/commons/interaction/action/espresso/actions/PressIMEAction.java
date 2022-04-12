package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.pressImeActionButton;

import android.os.Parcel;

import androidx.test.espresso.ViewAction;

public class PressIMEAction extends EspressoViewAction {
    public PressIMEAction() {
        super(EspressoViewActionType.PRESS_IME);
    }

    @Override
    public ViewAction getViewAction() {
        return pressImeActionButton();
    }

    @Override
    public String getCode() {
        return "pressImeActionButton()";
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
