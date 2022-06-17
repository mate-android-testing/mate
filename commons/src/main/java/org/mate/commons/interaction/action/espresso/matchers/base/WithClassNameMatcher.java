package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static org.hamcrest.Matchers.equalTo;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements an Espresso Matcher for targeting the views that have a certain Class name.
 */
public class WithClassNameMatcher extends EspressoViewMatcher {

    /**
     * The Class name to match against.
     */
    private final String className;

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
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        HashSet<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.matcher.ViewMatchers.withClassName");
        imports.add("org.hamcrest.Matchers.equalTo");
        return imports;
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
