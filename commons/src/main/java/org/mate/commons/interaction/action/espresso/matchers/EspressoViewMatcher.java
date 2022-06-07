package org.mate.commons.interaction.action.espresso.matchers;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.EspressoCodeProducer;
import org.mate.commons.interaction.action.espresso.matchers.base.IsRootMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.WithClassNameMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.WithContentDescriptionMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.WithHintMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.WithIdMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.WithTextMatcher;
import org.mate.commons.interaction.action.espresso.matchers.recursive.AllOfMatcher;
import org.mate.commons.interaction.action.espresso.matchers.recursive.AnyOfMatcher;
import org.mate.commons.interaction.action.espresso.matchers.recursive.HasDescendantMatcher;
import org.mate.commons.interaction.action.espresso.matchers.recursive.IsDescendantOfAMatcher;
import org.mate.commons.interaction.action.espresso.matchers.recursive.WithChildMatcher;
import org.mate.commons.interaction.action.espresso.matchers.recursive.WithParentMatcher;

public abstract class EspressoViewMatcher extends EspressoCodeProducer implements Parcelable {
    private EspressoViewMatcherType type;

    public EspressoViewMatcher(EspressoViewMatcherType type) {
        this.type = type;
    }

    public EspressoViewMatcherType getType() {
        return type;
    }

    public abstract Matcher<View> getViewMatcher();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
    }

    public static final Creator<EspressoViewMatcher> CREATOR = new Creator<EspressoViewMatcher>() {
        @Override
        public EspressoViewMatcher createFromParcel(Parcel source) {
            return EspressoViewMatcher.getConcreteClass(source);
        }

        @Override
        public EspressoViewMatcher[] newArray(int size) {
            return new EspressoViewMatcher[size];
        }
    };

    @Override
    public String toString() {
        return this.getCode();
    }

    /**
     * Auxiliary method to build an EspressoViewMatcher from a Parcel, using the correct subclass.
     * In order to do so, this method looks at the first integer in the Parcel.
     * Depending on the value, it will use the appropriate constructor from a subclass.
     *
     * DO NOT use here the CREATOR classes inside each of the EspressoViewMatcher subclasses.
     * Doing so will cause an infinite recursion, since they call this method in turn indirectly.
     *
     * @param source
     * @return
     */
    private static EspressoViewMatcher getConcreteClass(Parcel source) {
        int tmpType = source.readInt();
        EspressoViewMatcherType type = tmpType == -1 ? null :
                EspressoViewMatcherType.values()[tmpType];

        switch (type) {
            case IS_ROOT:
                return new IsRootMatcher(source);
            case WITH_CLASS_NAME:
                return new WithClassNameMatcher(source);
            case WITH_CONTENT_DESCRIPTION:
                return new WithContentDescriptionMatcher(source);
            case WITH_ID:
                return new WithIdMatcher(source);
            case WITH_TEXT:
                return new WithTextMatcher(source);
            case WITH_HINT:
                return new WithHintMatcher(source);
            case ALL_OF:
                return new AllOfMatcher(source);
            case ANY_OF:
                return new AnyOfMatcher(source);
            case HAS_DESCENDANT:
                return new HasDescendantMatcher(source);
            case IS_DESCENDANT_OF_A:
                return new IsDescendantOfAMatcher(source);
            case WITH_CHILD:
                return new WithChildMatcher(source);
            case WITH_PARENT:
                return new WithParentMatcher(source);
            default:
                throw new IllegalStateException("Invalid int for EspressoViewMatcher type found: " +
                        type);
        }
    }
}
