package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.click;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a Click Espresso action.
 */
public class ClickAction extends EspressoViewAction {
    public ClickAction() {
        super(EspressoViewActionType.CLICK);
    }

    @Override
    public ViewAction getViewAction() {
        return click();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        if (!view.isClickable()) {
            return false;
        }

        return getViewAction().getConstraints().matches(view);
    }

    @Override
    public String getCode() {
        return "click()";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.action.ViewActions.click");
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
