package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.scrollTo;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a Scroll-to Espresso action.
 */
public class ScrollToAction extends EspressoViewAction {
    public ScrollToAction() {
        super(EspressoViewActionType.SCROLL_TO);
    }

    @Override
    public ViewAction getViewAction() {
        return scrollTo();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        return getViewAction().getConstraints().matches(view);
    }

    @Override
    public String getCode() {
        return "scrollTo()";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.action.ViewActions.scrollTo");
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

    protected ScrollToAction(Parcel in) {
        this();
    }

    public static final Creator<ScrollToAction> CREATOR = new Creator<ScrollToAction>() {
        @Override
        public ScrollToAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (ScrollToAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public ScrollToAction[] newArray(int size) {
            return new ScrollToAction[size];
        }
    };
}
