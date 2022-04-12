package org.mate.commons.interaction.action.espresso.actions;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.test.espresso.ViewAction;

import org.mate.commons.interaction.action.espresso.EspressoCodeProducer;

public abstract class EspressoViewAction extends EspressoCodeProducer implements Parcelable {
    private final EspressoViewActionType type;

    public EspressoViewAction(EspressoViewActionType type) {
        this.type = type;
    }

    public EspressoViewActionType getType() {
        return type;
    }

    public abstract ViewAction getViewAction();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
    }

    public static final Creator<EspressoViewAction> CREATOR = new Creator<EspressoViewAction>() {
        @Override
        public EspressoViewAction createFromParcel(Parcel source) {
            return EspressoViewAction.getConcreteClass(source);
        }

        @Override
        public EspressoViewAction[] newArray(int size) {
            return new EspressoViewAction[size];
        }
    };

    /**
     * Auxiliary method to build an EspressoViewAction from a Parcel, using the correct subclass.
     * In order to do so, this method looks at the first integer in the Parcel.
     * Depending on the value, it will use the appropriate constructor from a subclass.
     *
     * DO NOT use here the CREATOR classes inside each of the EspressoViewAction subclasses.
     * Doing so will cause an infinite recursion, since they call this method in turn indirectly.
     *
     * @param source
     * @return
     */
    private static EspressoViewAction getConcreteClass(Parcel source) {
        int tmpType = source.readInt();
        EspressoViewActionType type = tmpType == -1 ? null : EspressoViewActionType.values()[tmpType];

        switch (type) {
            case CLICK:
                return new ClickAction(source);
            case DOUBLE_CLICK:
                return new DoubleClickAction(source);
            case LONG_CLICK:
                return new LongClickAction(source);
            case CLEAR_TEXT:
                return new ClearTextAction(source);
            case TYPE_TEXT:
                return new TypeTextAction(source);
            case SWIPE_UP:
                return new SwipeUpAction(source);
            case SWIPE_DOWN:
                return new SwipeDownAction(source);
            case SWIPE_LEFT:
                return new SwipeLeftAction(source);
            case SWIPE_RIGHT:
                return new SwipeRightAction(source);
            case SCROLL_TO:
                return new ScrollToAction(source);
            case BACK:
                return new BackAction(source);
            case MENU:
                return new MenuAction(source);
            case ENTER:
                return new EnterAction(source);
            case PRESS_IME:
                return new PressIMEAction(source);
            case CLOSE_SOFT_KEYBOARD:
                return new CloseSoftKeyboardAction(source);
            default:
                throw new IllegalStateException("Invalid int for EspressoViewAction type found: " +
                        type);
        }
    }
}
