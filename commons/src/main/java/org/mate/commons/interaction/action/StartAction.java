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
     * A simple textual representation. This should conform with
     * the analysis framework.
     *
     * @return Returns the string representation.
     */
    @NonNull
    @Override
    public String toString() {
        return "start action";
    }

    /**
     * Another simple text representation used for MATE's IGUIModel.
     *
     * @return Returns a simplified string representation.
     */
    @NonNull
    @Override
    public String toShortString() {
        return toString();
    }

    /**
     * Computes the hashcode of the ui action.
     *
     * @return Returns the hash code associated with the ui action.
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * Compares two ui action for equality.
     *
     * @param other The other ui action to compare against.
     * @return Returns {@code true} if both ui action are equal,
     * otherwise {@code false} is returned.
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
