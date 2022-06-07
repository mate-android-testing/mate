package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.swipeUp;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

public class SwipeUpAction extends EspressoViewAction {
    public SwipeUpAction() {
        super(EspressoViewActionType.SWIPE_UP);
    }

    @Override
    public ViewAction getViewAction() {
        return swipeUp();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        if (!view.isEnabled() || !view.canScrollVertically(-1)) {
            return false;
        }

        return getViewAction().getConstraints().matches(view);
    }

    @Override
    public String getCode() {
        return "swipeUp()";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected SwipeUpAction(Parcel in) {
        this();
    }

    public static final Creator<SwipeUpAction> CREATOR = new Creator<SwipeUpAction>() {
        @Override
        public SwipeUpAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (SwipeUpAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public SwipeUpAction[] newArray(int size) {
            return new SwipeUpAction[size];
        }
    };
}
