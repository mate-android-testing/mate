package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.swipeLeft;

import android.os.Parcel;

import androidx.test.espresso.ViewAction;

public class SwipeLeftAction extends EspressoViewAction {
    public SwipeLeftAction() {
        super(EspressoViewActionType.SWIPE_LEFT);
    }

    @Override
    public ViewAction getViewAction() {
        return swipeLeft();
    }

    @Override
    public String getCode() {
        return "swipeLeft()";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected SwipeLeftAction(Parcel in) {
        this();
    }

    public static final Creator<SwipeLeftAction> CREATOR = new Creator<SwipeLeftAction>() {
        @Override
        public SwipeLeftAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (SwipeLeftAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public SwipeLeftAction[] newArray(int size) {
            return new SwipeLeftAction[size];
        }
    };
}
