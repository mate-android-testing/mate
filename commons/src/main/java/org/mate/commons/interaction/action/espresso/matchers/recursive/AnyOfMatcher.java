package org.mate.commons.interaction.action.espresso.matchers.recursive;

import static org.hamcrest.Matchers.anyOf;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.List;

public class AnyOfMatcher extends EspressoViewMatcher {
    private List<EspressoViewMatcher> matchers;

    public AnyOfMatcher(List<EspressoViewMatcher> matchers) {
        super(EspressoViewMatcherType.ANY_OF);
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

        return String.format("anyOf(%s)", viewMatchers.toString());
    }

    @Override
    public Matcher<View> getViewMatcher() {
        Matcher<View>[] viewMatchers = new Matcher[matchers.size()];
        for (int i = 0; i < matchers.size(); i++) {
            viewMatchers[i] = matchers.get(i).getViewMatcher();
        }

        return anyOf(viewMatchers);
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

    public AnyOfMatcher(Parcel in) {
        this(in.createTypedArrayList(EspressoViewMatcher.CREATOR));
    }

    public static final Creator<AnyOfMatcher> CREATOR = new Creator<AnyOfMatcher>() {
        @Override
        public AnyOfMatcher createFromParcel(Parcel source) {
            // We need to use the EspressoViewMatcher.CREATOR here, because we want to make sure
            // to remove the EspressoViewMatcher's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (AnyOfMatcher) EspressoViewMatcher.CREATOR.createFromParcel(source);
        }

        @Override
        public AnyOfMatcher[] newArray(int size) {
            return new AnyOfMatcher[size];
        }
    };
}
