package org.mate.commons.interaction.action.espresso.matchers.recursive;

import static org.hamcrest.Matchers.allOf;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.List;

public class AllOfMatcher extends EspressoViewMatcher {
    private List<EspressoViewMatcher> matchers;

    public AllOfMatcher(List<EspressoViewMatcher> matchers) {
        super(EspressoViewMatcherType.ALL_OF);
        this.matchers = matchers;
    }

    @Override
    public String getCode() {
        StringBuilder viewMatchers = new StringBuilder();
        for (EspressoViewMatcher matcher : matchers) {
            viewMatchers.append(String.format("%s, ", matcher.getCode()));
        }

        // Delete last comma and space
        viewMatchers.deleteCharAt(viewMatchers.length() - 1);
        viewMatchers.deleteCharAt(viewMatchers.length() - 1);

        return String.format("allOf(%s)", viewMatchers.toString());
    }

    @Override
    public Matcher<View> getViewMatcher() {
        Matcher<View>[] viewMatchers = new Matcher[matchers.size()];
        for (int i = 0; i < matchers.size(); i++) {
            viewMatchers[i] = matchers.get(i).getViewMatcher();
        }

        return allOf(viewMatchers);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(this.matchers);
    }

    public AllOfMatcher(Parcel in) {
        this(in.createTypedArrayList(EspressoViewMatcher.CREATOR));
    }

    public static final Creator<AllOfMatcher> CREATOR = new Creator<AllOfMatcher>() {
        @Override
        public AllOfMatcher createFromParcel(Parcel source) {
            // We need to use the EspressoViewMatcher.CREATOR here, because we want to make sure
            // to remove the EspressoViewMatcher's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (AllOfMatcher) EspressoViewMatcher.CREATOR.createFromParcel(source);
        }

        @Override
        public AllOfMatcher[] newArray(int size) {
            return new AllOfMatcher[size];
        }
    };
}
