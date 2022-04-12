package org.mate.commons.interaction.action.espresso.matchers.recursive;

import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class IsDescendantOfAMatcher extends EspressoViewMatcher {
    private EspressoViewMatcher matcher;

    public IsDescendantOfAMatcher(EspressoViewMatcher matcher) {
        super(EspressoViewMatcherType.IS_DESCENDANT_OF_A);
        this.matcher = matcher;
    }

    @Override
    public String getCode() {
        return String.format("isDescendantOfA(%s)", matcher.getCode());
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return isDescendantOfA(matcher.getViewMatcher());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.matcher, flags);
    }

    public IsDescendantOfAMatcher(Parcel in) {
        this((EspressoViewMatcher) in.readParcelable(EspressoViewMatcher.class.getClassLoader()));
    }

    public static final Creator<IsDescendantOfAMatcher> CREATOR = new Creator<IsDescendantOfAMatcher>() {
        @Override
        public IsDescendantOfAMatcher createFromParcel(Parcel source) {
            // We need to use the EspressoViewMatcher.CREATOR here, because we want to make sure
            // to remove the EspressoViewMatcher's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (IsDescendantOfAMatcher) EspressoViewMatcher.CREATOR.createFromParcel(source);
        }

        @Override
        public IsDescendantOfAMatcher[] newArray(int size) {
            return new IsDescendantOfAMatcher[size];
        }
    };
}
