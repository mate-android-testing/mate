package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.HashSet;
import java.util.Set;

public class WithResourceNameMatcher extends EspressoViewMatcher {
    private String resourceName;

    public WithResourceNameMatcher(String resourceName) {
        super(EspressoViewMatcherType.WITH_RESOURCE_NAME);
        this.resourceName = resourceName;
    }

    @Override
    public String getCode() {
        return String.format("withResourceName(%s)", boxString(resourceName));
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return withResourceName(resourceName);
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        HashSet<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.matcher.ViewMatchers.withResourceName");
        return imports;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.resourceName);
    }

    public WithResourceNameMatcher(Parcel in) {
        this(in.readString());
    }

    public static final Creator<WithResourceNameMatcher> CREATOR = new Creator<WithResourceNameMatcher>() {
        @Override
        public WithResourceNameMatcher createFromParcel(Parcel source) {
            // We need to use the EspressoViewMatcher.CREATOR here, because we want to make sure
            // to remove the EspressoViewMatcher's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (WithResourceNameMatcher) EspressoViewMatcher.CREATOR.createFromParcel(source);
        }

        @Override
        public WithResourceNameMatcher[] newArray(int size) {
            return new WithResourceNameMatcher[size];
        }
    };
}
