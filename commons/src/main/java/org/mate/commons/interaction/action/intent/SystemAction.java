package org.mate.commons.interaction.action.intent;

import android.os.Parcel;
import android.support.annotation.NonNull;

import org.mate.commons.interaction.action.Action;

import java.util.Objects;

/**
 * Describes a system event notification that should be broad-casted to a certain receiver component.
 */
public class SystemAction extends Action {

    /**
     * The component describing the receiver of the system action.
     */
    private final ComponentDescription component;

    /**
     * The intent-filter of the receiver.
     */
    private final IntentFilterDescription intentFilter;

    /**
     * The name of the receiver.
     */
    private final String receiver;

    /**
     * Whether we deal with a dynamic broadcast receiver or not.
     */
    private boolean dynamic = false;

    /**
     * The action that the receiver expects.
     */
    private final String action;

    // TODO: certain system events may also require a category, which should be included in the intent
    //  likewise data tags (URIs) could be relevant

    /**
     * Initialises a system event.
     *
     * @param component The component definition representing the broadcast receiver.
     * @param intentFilter The selected intent filter.
     * @param action The name of the system event the receiver is listening for.
     */
    public SystemAction(ComponentDescription component,
                        IntentFilterDescription intentFilter, String action) {
        this.component = component;
        this.intentFilter = intentFilter;
        this.receiver = component.getFullyQualifiedName();
        this.action = action;
    }

    /**
     * Marks the receiver as a dynamic one.
     */
    public void markAsDynamic() {
        dynamic = true;
    }

    /**
     * Returns whether the encapsulated receiver is dynamic or not.
     *
     * @return Returns {@code true} if the receiver is a dynamic receiver,
     *          otherwise {@code false} is returned.
     */
    public boolean isDynamicReceiver() {
        return dynamic;
    }

    /**
     * Returns the name of the broadcast receiver.
     *
     * @return Returns the name of the broadcast receiver.
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * Returns the action that the broadcast receiver expects.
     *
     * @return Returns the action that the broadcast receiver expects.
     */
    public String getAction() {
        return action;
    }

    /**
     * Defines a custom representation of a system action. Do not alter this representation without
     * changing the parsing routine of the analysis framework!
     *
     * @return Returns the string representation of a system action.
     */
    @NonNull
    @Override
    public String toString() {
        return "system action: act=" + action + " cmp=" + receiver;
    }

    /**
     * Defines an equality metric for two system actions. In our context, two system actions are
     * identical if they refer to the same (dynamic) receiver and contain the same action.
     *
     * @param o The other system action to be checked for equality.
     * @return Returns {@code true} if two system actions are identical, otherwise {@code false}
     *      is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            SystemAction that = (SystemAction) o;
            return dynamic == that.dynamic &&
                    Objects.equals(receiver, that.receiver) &&
                    Objects.equals(action, that.action);
        }
    }

    /**
     * Computes the hash code of the system action.
     *
     * @return Returns the hash code of the system action.
     */
    @Override
    public int hashCode() {
        return Objects.hash(receiver, dynamic, action);
    }

    /**
     * Returns a short string representation used by the {@link org.mate.model.IGUIModel}.
     *
     * @return Returns a short string representation.
     */
    @NonNull
    @Override
    public String toShortString() {
        return action;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(ACTION_TYPE_SYSTEM);

        dest.writeParcelable(this.component, flags);
        dest.writeParcelable(this.intentFilter, flags);
        dest.writeString(this.receiver);
        dest.writeByte(this.dynamic ? (byte) 1 : (byte) 0);
        dest.writeString(this.action);
    }

    public SystemAction(Parcel in) {
        this.component = in.readParcelable(ComponentDescription.class.getClassLoader());
        this.intentFilter = in.readParcelable(IntentFilterDescription.class.getClassLoader());
        this.receiver = in.readString();
        this.dynamic = in.readByte() != 0;
        this.action = in.readString();
    }

    public static final Creator<SystemAction> CREATOR = new Creator<SystemAction>() {
        @Override
        public SystemAction createFromParcel(Parcel source) {
            return new SystemAction(source);
        }

        @Override
        public SystemAction[] newArray(int size) {
            return new SystemAction[size];
        }
    };
}
