package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withHint;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements an Espresso Matcher for targeting the views that have a certain hint.
 */
public class WithHintMatcher extends EspressoViewMatcher {

    /**
     * The hint to match against.
     */
    private final String hint;

    public WithHintMatcher(String hint) {
        super(EspressoViewMatcherType.WITH_HINT);
        this.hint = hint;
    }

    @Override
    public String getCode() {
        return String.format("withHint(%s)", boxString(hint));
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withHint(hint);
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        HashSet<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.matcher.ViewMatchers.withHint");
        return imports;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.hint);
    }

    public WithHintMatcher(Parcel in) {
        this(in.readString());
    }

    public static final Creator<WithHintMatcher> CREATOR = new Creator<WithHintMatcher>() {
        @Override
        public WithHintMatcher createFromParcel(Parcel source) {
            // We need to use the EspressoViewMatcher.CREATOR here, because we want to make sure
            // to remove the EspressoViewMatcher's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (WithHintMatcher) EspressoViewMatcher.CREATOR.createFromParcel(source);
        }

        @Override
        public WithHintMatcher[] newArray(int size) {
            return new WithHintMatcher[size];
        }
    };
}
