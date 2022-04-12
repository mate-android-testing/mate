package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static org.hamcrest.Matchers.equalTo;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithClassNameMatcher extends EspressoViewMatcher {
    private String className;

    public WithClassNameMatcher(String className) {
        super(EspressoViewMatcherType.WITH_CLASS_NAME);
        this.className = className;
    }

    @Override
    public String getCode() {
        return String.format("withClassName(equalTo(%s))", boxString(className));
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withClassName(equalTo(className));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.className);
    }

    public WithClassNameMatcher(Parcel in) {
        this(in.readString());
    }

    public static final Creator<WithClassNameMatcher> CREATOR = new Creator<WithClassNameMatcher>() {
        @Override
        public WithClassNameMatcher createFromParcel(Parcel source) {
            // We need to use the EspressoViewMatcher.CREATOR here, because we want to make sure
            // to remove the EspressoViewMatcher's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (WithClassNameMatcher) EspressoViewMatcher.CREATOR.createFromParcel(source);
        }

        @Override
        public WithClassNameMatcher[] newArray(int size) {
            return new WithClassNameMatcher[size];
        }
    };
}
