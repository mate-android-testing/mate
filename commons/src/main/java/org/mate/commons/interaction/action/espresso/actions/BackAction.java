package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.pressBackUnconditionally;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

public class BackAction extends EspressoViewAction {
    public BackAction() {
        super(EspressoViewActionType.BACK);
    }

    @Override
    public ViewAction getViewAction() {
        return pressBackUnconditionally();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        // This action can only be performed on the root view.
        return isRoot().matches(view);
    }

    @Override
    public String getCode() {
        return "pressBackUnconditionally()";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected BackAction(Parcel in) {
        this();
    }

    public static final Creator<BackAction> CREATOR = new Creator<BackAction>() {
        @Override
        public BackAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (BackAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public BackAction[] newArray(int size) {
            return new BackAction[size];
        }
    };
}
