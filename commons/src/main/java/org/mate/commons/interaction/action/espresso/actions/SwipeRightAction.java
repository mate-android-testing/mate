package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.swipeRight;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a Swipe right Espresso action.
 */
public class SwipeRightAction extends EspressoViewAction {
    public SwipeRightAction() {
        super(EspressoViewActionType.SWIPE_RIGHT);
    }

    @Override
    public ViewAction getViewAction() {
        return swipeRight();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        if (!view.canScrollHorizontally(-1)) {
            return false;
        }

        return getViewAction().getConstraints().matches(view);
    }

    @Override
    public String getCode() {
        return "swipeRight()";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.action.ViewActions.swipeRight");
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

    protected SwipeRightAction(Parcel in) {
        this();
    }

    public static final Creator<SwipeRightAction> CREATOR = new Creator<SwipeRightAction>() {
        @Override
        public SwipeRightAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (SwipeRightAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public SwipeRightAction[] newArray(int size) {
            return new SwipeRightAction[size];
        }
    };
}
