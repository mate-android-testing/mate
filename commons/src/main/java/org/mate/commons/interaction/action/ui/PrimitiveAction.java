package org.mate.commons.interaction.action.ui;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mate.commons.interaction.action.Action;
import org.mate.commons.utils.Randomness;

import java.util.Arrays;
import java.util.Objects;

/**
 * A primitive action is not associated with a specific widget, but rather to (random) coordinates
 * x and y. Hence, a primitive action can be applied in any circumstance, but it may has no effect
 * if x and y don't refer to the location of a widget.
 */
public class PrimitiveAction extends UIAction {

    /**
     * The x and y coordinates at which the action should be applied.
     */
    private int x, y;

    /**
     * The text that was inserted to a possible input field at position x and y.
     * We need to save the text in order to allow a deterministic replaying of actions.
     */
    private String text;

    /**
     * Initialises a primitive actions that should be applied at the given x and y coordinates.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param actionType The type of action, e.g. click.
     * @param activity The current activity.
     */
    public PrimitiveAction(int x, int y, ActionType actionType, String activity) {
        super(actionType, activity);
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x coordinate.
     *
     * @return Returns the x coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y coordinate.
     *
     * @return Returns the y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the text that was used for a input field.
     *
     * @return Returns the text that was used for a input field or {@code null} if not set yet.
     */
    public String getText() {
        return text;
    }

    /**
     * Records the text that was used for an input field.
     *
     * @param text The text that was used for an input field.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Generates a random primitive action, i.e. a random action on some random coordinates.
     *
     * @return Returns a randomly generated primitive action.
     */
    public static PrimitiveAction randomAction(String activity, int screenWidth, int screenHeight) {
        int x = Randomness.getRnd().nextInt(screenWidth);
        int y = Randomness.getRnd().nextInt(screenHeight);
        return new PrimitiveAction(x, y,
                Randomness.randomElement(Arrays.asList(ActionType.primitiveActionTypes)), activity);
    }

    /**
     * Compares two primitive actions for equality.
     *
     * @param o The object to which we compare.
     * @return Returns {@code true} if both actions are equal,
     *          otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            PrimitiveAction other = (PrimitiveAction) o;
            return this.x == other.x && this.y == other.y && actionType == other.actionType;
        }
    }

    /**
     * Computes the hash code based on attributes used for {@link #equals(Object)}.
     *
     * @return Returns the associated hash code of the primitive action.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y, actionType);
    }

    /**
     * The string representation used in combination with analysis framework.
     *
     * @return Returns the string representation of a primitive action.
     */
    @NonNull
    @Override
    public String toString() {
        return "primitive action: " + actionType + " at (" + x + "," + y + ")";
    }

    /**
     * A simplified textual representation used for the {@link org.mate.model.IGUIModel}.
     *
     * @return Returns a simplified string representation.
     */
    @NonNull
    @Override
    public String toShortString() {
        return actionType + " at (" + x + "," + y + ")";
    }

    @Override
    public int getIntForActionSubClass() {
        return ACTION_SUBCLASS_PRIMITIVE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.x);
        dest.writeInt(this.y);
        dest.writeString(this.text);
    }

    public PrimitiveAction(Parcel in) {
        super(in);
        this.x = in.readInt();
        this.y = in.readInt();
        this.text = in.readString();
    }

    public static final Creator<PrimitiveAction> CREATOR = new Creator<PrimitiveAction>() {
        @Override
        public PrimitiveAction createFromParcel(Parcel source) {
            // We need to use the Action.CREATOR here, because we want to make sure to remove the
            // ActionSubClass integer from the beginning of Parcel and call the appropriate
            // constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (PrimitiveAction) Action.CREATOR.createFromParcel(source);
        }

        @Override
        public PrimitiveAction[] newArray(int size) {
            return new PrimitiveAction[size];
        }
    };
}
