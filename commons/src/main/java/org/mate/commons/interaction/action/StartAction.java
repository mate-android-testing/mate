package org.mate.commons.interaction.action;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Represent a virtual action that transitions the GUIModel from the virtual start state into the
 * initial state of the AUT after a restart.
 */
public final class StartAction extends Action {

    public static final Creator<StartAction> CREATOR = new Creator<StartAction>() {
        @Override
        public StartAction createFromParcel(Parcel source) {
            // We need to use the Action.CREATOR here, because we want to make sure to remove the
            // ActionSubClass integer from the beginning of Parcel and call the appropriate
            // constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (StartAction) Action.CREATOR.createFromParcel(source);
        }

        @Override
        public StartAction[] newArray(int size) {
            return new StartAction[size];
        }
    };

    /**
     * Constructs a new StartAction.
     */
    public StartAction() {
    }

    public StartAction(Parcel in) {
        super(in);
    }

    /**
     * Provides a textual representation of the virtual start action.
     *
     * @return Returns the string representation of the virtual start action.
     */
    @NonNull
    @Override
    public String toString() {
        return "start action";
    }

    /**
     * Another simple text representation used for the gui model.
     *
     * @return Returns a simplified string representation.
     */
    @NonNull
    @Override
    public String toShortString() {
        return toString();
    }

    /**
     * Computes the hash code for the given action.
     *
     * @return Returns the hash code for the given action.
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * Compares two {@link Action}s for equality.
     *
     * @param other The other action.
     * @return Returns {@code true} if both action represent a start action, otherwise {@code false}
     *         is returned.
     */
    @Override
    public boolean equals(@Nullable Object other) {
        return other != null && getClass().equals(other.getClass());
    }

    @Override
    public int getIntForActionSubClass() {
        return ACTION_SUBCLASS_START;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }
}
