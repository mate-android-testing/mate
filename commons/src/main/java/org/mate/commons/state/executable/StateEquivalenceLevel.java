package org.mate.commons.state.executable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Defines the various supported abstractions used in the {@link org.mate.state.IScreenState}
 * equality comparison.
 */
public enum StateEquivalenceLevel implements Parcelable {

    /**
     * Two {@link org.mate.state.IScreenState}s are equal if they share the same package name. This
     * is the weakest abstraction level and implies that all screen states of the app are treated
     * as one model state.
     */
    PACKAGE_NAME,

    /**
     * Two {@link org.mate.state.IScreenState}s are equal if they refer to the same package and
     * activity. This means that each activity defines an own model state.
     */
    ACTIVITY_NAME,

    /**
     * Two {@link org.mate.state.IScreenState}s are equal if they refer to the same package and
     * activity and contain the same number of widgets and the widgets are at the same position.
     * This is the default abstraction level.
     */
    WIDGET,

    /**
     * Two {@link org.mate.state.IScreenState}s are equal if they refer to the same package and
     * activity and contain the same widgets, where the content description and the text within a
     * text field of a widget are considered as well.
     */
    WIDGET_WITH_ATTRIBUTES;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ordinal());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StateEquivalenceLevel> CREATOR = new Creator<StateEquivalenceLevel>() {
        @Override
        public StateEquivalenceLevel createFromParcel(Parcel in) {
            int ordinal = in.readInt();
            return StateEquivalenceLevel.values()[ordinal];
        }

        @Override
        public StateEquivalenceLevel[] newArray(int size) {
            return new StateEquivalenceLevel[size];
        }
    };
}
