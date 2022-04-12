package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.swipeDown;

import android.os.Parcel;

import androidx.test.espresso.ViewAction;

public class SwipeDownAction extends EspressoViewAction {
    public SwipeDownAction() {
        super(EspressoViewActionType.SWIPE_DOWN);
    }

    @Override
    public ViewAction getViewAction() {
        return swipeDown();
    }

    @Override
    public String getCode() {
        return "swipeDown()";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected SwipeDownAction(Parcel in) {
        this();
    }

    public static final Creator<SwipeDownAction> CREATOR = new Creator<SwipeDownAction>() {
        @Override
        public SwipeDownAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (SwipeDownAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public SwipeDownAction[] newArray(int size) {
            return new SwipeDownAction[size];
        }
    };
}
