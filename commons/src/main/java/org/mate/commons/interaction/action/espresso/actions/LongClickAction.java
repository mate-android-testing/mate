package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.longClick;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a Long click Espresso action.
 */
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
        if (!view.isLongClickable()) {
            return false;
        }

        return getViewAction().getConstraints().matches(view);
    }

    @Override
    public String getCode() {
        return "longClick()";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.action.ViewActions.longClick");
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
