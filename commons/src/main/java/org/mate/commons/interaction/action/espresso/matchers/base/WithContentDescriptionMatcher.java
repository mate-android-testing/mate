package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

public class WithContentDescriptionMatcher extends EspressoViewMatcher {
    private String contentDescription;

    public WithContentDescriptionMatcher(String contentDescription) {
        super(EspressoViewMatcherType.WITH_CONTENT_DESCRIPTION);
        this.contentDescription = contentDescription;
    }

    @Override
    public String getCode() {
        return String.format("withContentDescription(%s)", boxString(contentDescription));
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withContentDescription(contentDescription);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.contentDescription);
    }

    public WithContentDescriptionMatcher(Parcel in) {
        this(in.readString());
    }

    public static final Creator<WithContentDescriptionMatcher> CREATOR = new Creator<WithContentDescriptionMatcher>() {
        @Override
        public WithContentDescriptionMatcher createFromParcel(Parcel source) {
            // We need to use the EspressoViewMatcher.CREATOR here, because we want to make sure
            // to remove the EspressoViewMatcher's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (WithContentDescriptionMatcher) EspressoViewMatcher.CREATOR.createFromParcel(source);
        }

        @Override
        public WithContentDescriptionMatcher[] newArray(int size) {
            return new WithContentDescriptionMatcher[size];
        }
    };
}
