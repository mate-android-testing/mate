package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.typeText;

import android.os.Parcel;

import androidx.test.espresso.ViewAction;

public class TypeTextAction extends EspressoViewAction {
    private String stringToBeTyped;

    public TypeTextAction(String stringToBeTyped) {
        super(EspressoViewActionType.TYPE_TEXT);
        this.stringToBeTyped = stringToBeTyped;
    }

    @Override
    public ViewAction getViewAction() {
        if (!stringToBeTyped.endsWith("\n")) {
            // Appending a \n to the end of the string translates to a ENTER key event.
            stringToBeTyped += "\n";
        }

        return typeText(stringToBeTyped);
    }

    @Override
    public String getCode() {
        return String.format("typeText(%s)", boxString(stringToBeTyped));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.stringToBeTyped);
    }

    protected TypeTextAction(Parcel in) {
        this(in.readString());
    }

    public static final Creator<TypeTextAction> CREATOR = new Creator<TypeTextAction>() {
        @Override
        public TypeTextAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (TypeTextAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public TypeTextAction[] newArray(int size) {
            return new TypeTextAction[size];
        }
    };
}
