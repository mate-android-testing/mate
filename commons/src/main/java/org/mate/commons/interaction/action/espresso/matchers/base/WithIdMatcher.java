package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements an Espresso Matcher for targeting the views that have a certain ID.
 */
public class WithIdMatcher extends EspressoViewMatcher {

    /**
     * The ID to match against.
     */
    private final int id;

    public WithIdMatcher(int id) {
        super(EspressoViewMatcherType.WITH_ID);
        this.id = id;
    }

    @Override
    public String getCode() {
        return String.format("withId(%d)", id);
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withId(id);
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        HashSet<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.matcher.ViewMatchers.withId");
        return imports;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.id);
    }

    public WithIdMatcher(Parcel in) {
        this(in.readInt());
    }

    public static final Creator<WithIdMatcher> CREATOR = new Creator<WithIdMatcher>() {
        @Override
        public WithIdMatcher createFromParcel(Parcel source) {
            // We need to use the EspressoViewMatcher.CREATOR here, because we want to make sure
            // to remove the EspressoViewMatcher's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (WithIdMatcher) EspressoViewMatcher.CREATOR.createFromParcel(source);
        }

        @Override
        public WithIdMatcher[] newArray(int size) {
            return new WithIdMatcher[size];
        }
    };
}
