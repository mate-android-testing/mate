package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements an Espresso Matcher for targeting the views that have a certain text.
 */
public class WithTextMatcher extends EspressoViewMatcher {

    /**
     * The text to match against.
     */
    private final String text;

    public WithTextMatcher(String text) {
        super(EspressoViewMatcherType.WITH_TEXT);
        this.text = text;
    }

    @Override
    public String getCode() {
        return String.format("withText(%s)", boxString(text));
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withText(text);
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        HashSet<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.matcher.ViewMatchers.withText");
        return imports;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.text);
    }

    public WithTextMatcher(Parcel in) {
        this(in.readString());
    }

    public static final Creator<WithTextMatcher> CREATOR = new Creator<WithTextMatcher>() {
        @Override
        public WithTextMatcher createFromParcel(Parcel source) {
            // We need to use the EspressoViewMatcher.CREATOR here, because we want to make sure
            // to remove the EspressoViewMatcher's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (WithTextMatcher) EspressoViewMatcher.CREATOR.createFromParcel(source);
        }

        @Override
        public WithTextMatcher[] newArray(int size) {
            return new WithTextMatcher[size];
        }
    };
}
