package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.longClick;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

public class LongClickAction extends EspressoViewAction {
    public LongClickAction() {
        super(EspressoViewActionType.LONG_CLICK);
    }

    @Override
    public ViewAction getViewAction() {
        return longClick();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        if (!view.isEnabled() || !view.isLongClickable()) {
            return false;
        }

        return getViewAction().getConstraints().matches(view);
    }

    @Override
    public String getCode() {
        return "longClick()";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected LongClickAction(Parcel in) {
        this();
    }

    public static final Creator<LongClickAction> CREATOR = new Creator<LongClickAction>() {
        @Override
        public LongClickAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (LongClickAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public LongClickAction[] newArray(int size) {
            return new LongClickAction[size];
        }
    };
}
