package org.mate.commons.interaction.action;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mate.commons.interaction.action.intent.IntentBasedAction;
import org.mate.commons.interaction.action.intent.SystemAction;
import org.mate.commons.interaction.action.ui.MotifAction;
import org.mate.commons.interaction.action.ui.PrimitiveAction;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.interaction.action.ui.WidgetAction;

public abstract class Action implements Parcelable {

    public static final int ACTION_SUBCLASS_WIDGET = 1;
    public static final int ACTION_SUBCLASS_PRIMITIVE = 2;
    public static final int ACTION_SUBCLASS_INTENT_BASED = 3;
    public static final int ACTION_SUBCLASS_SYSTEM = 4;
    public static final int ACTION_SUBCLASS_MOTIF = 5;
    public static final int ACTION_SUBCLASS_UI = 6;
    public static final int ACTION_SUBCLASS_START = 7;

    /**
     * A detailed description of the action. Primarily used
     * for the analysis framework.
     *
     * @return Returns a detailed string representation.
     */
    @NonNull
    public abstract String toString();

    /**
     * A simplified description of the action. Primarily used
     * for the gui model.
     *
     * @return Returns a simplified string representation.
     */
    @NonNull
    public abstract String toShortString();

    public abstract int hashCode();

    public abstract boolean equals(@Nullable Object o);

    public abstract int getIntForActionSubClass();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Action class does not have protected variables, but we need to write an integer to
        // identify which of the many Action subclasses we are converting to Parcel
        int intForActionSubClass = getIntForActionSubClass();
        dest.writeInt(intForActionSubClass);
    }

    public Action() {}

    protected Action(Parcel in) {
        // nothing to do, since Action class does not have protected variables
    }

    public static final Creator<Action> CREATOR = new Creator<Action>() {
        @Override
        public Action createFromParcel(Parcel source) {
            return Action.getConcreteClass(source);
        }

        @Override
        public Action[] newArray(int size) {
            return new Action[size];
        }
    };

    /**
     * Auxiliary method to build an action from a Parcel, using the correct subclass.
     * In order to do so, this method looks at the first integer in the Parcel.
     * Depending on the value, it will use the appropriate constructor from a subclass.
     *
     * DO NOT use here the CREATOR classes inside each of the Action subclasses.
     * Doing so will cause an infinite recursion, since they call this method in turn indirectly.
     *
     * @param source
     * @return
     */
    private static Action getConcreteClass(Parcel source) {
        int intForActionSubClass = source.readInt();

        switch (intForActionSubClass) {
            case ACTION_SUBCLASS_WIDGET:
                return new WidgetAction(source);
            case ACTION_SUBCLASS_PRIMITIVE:
                return new PrimitiveAction(source);
            case ACTION_SUBCLASS_INTENT_BASED:
                return new IntentBasedAction(source);
            case ACTION_SUBCLASS_SYSTEM:
                return new SystemAction(source);
            case ACTION_SUBCLASS_MOTIF:
                return new MotifAction(source);
            case ACTION_SUBCLASS_UI:
                return new UIAction(source);
            case ACTION_SUBCLASS_START:
                return new StartAction(source);
            default:
                throw new IllegalStateException("Invalid int for Action subclass found: " +
                        intForActionSubClass);
        }
    }
}
