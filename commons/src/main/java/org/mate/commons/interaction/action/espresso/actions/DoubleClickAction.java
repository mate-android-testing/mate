package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.doubleClick;

import android.os.Parcel;

import androidx.test.espresso.ViewAction;

public class DoubleClickAction extends EspressoViewAction {
    public DoubleClickAction() {
        super(EspressoViewActionType.DOUBLE_CLICK);
    }

    @Override
    public ViewAction getViewAction() {
        return doubleClick();
    }

    @Override
    public String getCode() {
        return "doubleClick()";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected DoubleClickAction(Parcel in) {
        this();
    }

    public static final Creator<DoubleClickAction> CREATOR = new Creator<DoubleClickAction>() {
        @Override
        public DoubleClickAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (DoubleClickAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public DoubleClickAction[] newArray(int size) {
            return new DoubleClickAction[size];
        }
    };
}
