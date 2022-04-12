package org.mate.commons.interaction.action.espresso;

import static androidx.test.espresso.Espresso.onView;

import android.os.Parcel;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.ViewAction;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewAction;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;

/**
 * An Espresso action is composed of a ViewMatcher (that tells Espresso which is the target view)
 * and a ViewAction (that tells Espresso what action to perform on the target view).
 */
public class EspressoAction extends Action {

    private EspressoViewAction espressoViewAction;
    private EspressoViewMatcher espressoViewMatcher;

    public EspressoAction(EspressoViewAction espressoViewAction,
                          EspressoViewMatcher espressoViewMatcher) {
        this.espressoViewAction = espressoViewAction;
        this.espressoViewMatcher = espressoViewMatcher;
    }

    public boolean execute() {
        try {
            Matcher<View> viewMatcher = espressoViewMatcher.getViewMatcher();
            ViewAction viewAction = espressoViewAction.getViewAction();
            onView(viewMatcher).perform(viewAction);
            return true;
        } catch (Exception e) {
            // do nothing
        }

        return false;
    }

    public String getCode() {
        String viewMatcherCode = espressoViewMatcher.getCode();
        String viewActionCode = espressoViewAction.getCode();
        String code = String.format("onView(%s).perform(%s)", viewMatcherCode, viewActionCode);

        return code;
    }

    /**
     * Compares two Espresso actions for equality.
     *
     * @param o The object to which we compare.
     * @return Returns {@code true} if both actions are equal,
     * otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            EspressoAction other = (EspressoAction) o;
            return espressoViewAction == other.espressoViewAction && espressoViewMatcher.equals(other.espressoViewMatcher);
        }
    }

    /**
     * Computes the hash code based on attributes used for {@link #equals(Object)}.
     *
     * @return Returns the associated hash code of the Espresso action.
     */
    @Override
    public int hashCode() {
        return espressoViewAction.hashCode() + espressoViewMatcher.hashCode();
    }

    /**
     * The string representation of the Espresso action.
     */
    @NonNull
    @Override
    public String toString() {
        return String.format("onView(%s).perform(%s)",
                espressoViewMatcher.toString(), espressoViewAction.toString());
    }

    @NonNull
    @Override
    public String toShortString() {
        return toString();
    }

    @Override
    public int getIntForActionSubClass() {
        return ACTION_SUBCLASS_ESPRESSO;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.espressoViewAction, flags);
        dest.writeParcelable(this.espressoViewMatcher, flags);
    }

    public EspressoAction(Parcel in) {
        super(in);
        this.espressoViewAction = in.readParcelable(EspressoViewAction.class.getClassLoader());
        this.espressoViewMatcher = in.readParcelable(EspressoViewMatcher.class.getClassLoader());
    }

    public static final Creator<EspressoAction> CREATOR = new Creator<EspressoAction>() {
        @Override
        public EspressoAction createFromParcel(Parcel source) {
            return new EspressoAction(source);
        }

        @Override
        public EspressoAction[] newArray(int size) {
            return new EspressoAction[size];
        }
    };
}
