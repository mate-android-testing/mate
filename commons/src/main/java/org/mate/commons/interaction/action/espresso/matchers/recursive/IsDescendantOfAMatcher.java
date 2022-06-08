package org.mate.commons.interaction.action.espresso.matchers.recursive;

import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IsDescendantOfAMatcher extends MultipleRecursiveMatcher {

    private AllOfMatcher allOfMatcher;

    public IsDescendantOfAMatcher() {
        this(new ArrayList<>());
    }

    public IsDescendantOfAMatcher(List<EspressoViewMatcher> matchers) {
        super(EspressoViewMatcherType.HAS_DESCENDANT);
        this.matchers = matchers;
        allOfMatcher = new AllOfMatcher(matchers);
    }

    @Override
    public void addMatcher(EspressoViewMatcher matcher) {
        super.addMatcher(matcher);
        this.allOfMatcher = new AllOfMatcher(matchers);
    }

    @Override
    public String getCode() {
        return String.format("isDescendantOfA(%s)", allOfMatcher.getCode());
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return isDescendantOfA(allOfMatcher.getViewMatcher());
    }

    @Override
    public Set<String> getNeededClassImports() {
        return allOfMatcher.getNeededClassImports();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        HashSet<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA");
        imports.addAll(allOfMatcher.getNeededStaticImports());
        return imports;
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

    public IsDescendantOfAMatcher(Parcel in) {
        this(in.createTypedArrayList(EspressoViewMatcher.CREATOR));
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
