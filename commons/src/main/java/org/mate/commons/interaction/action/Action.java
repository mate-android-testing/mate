package org.mate.commons.interaction.action;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mate.commons.interaction.action.intent.IntentBasedAction;
import org.mate.commons.interaction.action.intent.SystemAction;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.interaction.action.ui.WidgetAction;

public abstract class Action implements Parcelable {

    public static final int ACTION_SUBCLASS_WIDGET = 1;
    public static final int ACTION_SUBCLASS_PRIMITIVE = 2;
    public static final int ACTION_SUBCLASS_INTENT_BASED = 3;
    public static final int ACTION_SUBCLASS_SYSTEM = 4;
    public static final int ACTION_SUBCLASS_MOTIF = 5;
    public static final int ACTION_SUBCLASS_UI = 6;

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
        dest.writeInt(getIntForActionSubClass());
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

    private static Action getConcreteClass(Parcel source) {
        switch (source.readInt()) {
            case ACTION_SUBCLASS_WIDGET:
                return new WidgetAction(source);
            // case ACTION_TYPE_PRIMITIVE:
            //     return new PrimitiveAction(source);
            case ACTION_SUBCLASS_INTENT_BASED:
                return new IntentBasedAction(source);
            case ACTION_SUBCLASS_SYSTEM:
                return new SystemAction(source);
            // case ACTION_TYPE_MOTIF:
            //     return new MotifAction(source);
            case ACTION_SUBCLASS_UI:
                return new UIAction(source);
            default:
                return null;
        }
    }
}
