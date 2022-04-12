package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.click;

import android.os.Parcel;

import androidx.test.espresso.ViewAction;

public class ClickAction extends EspressoViewAction {
    public ClickAction() {
        super(EspressoViewActionType.CLICK);
    }

    @Override
    public ViewAction getViewAction() {
        return click();
    }

    @Override
    public String getCode() {
        return "click()";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected ClickAction(Parcel in) {
        this();
    }

    public static final Creator<ClickAction> CREATOR = new Creator<ClickAction>() {
        @Override
        public ClickAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (ClickAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public ClickAction[] newArray(int size) {
            return new ClickAction[size];
        }
    };
}
