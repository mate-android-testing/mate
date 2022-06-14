package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.typeText;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

import java.util.HashSet;
import java.util.Set;

public class TypeTextAction extends EspressoViewAction {
    private String stringToBeTyped;

    public TypeTextAction(String stringToBeTyped) {
        super(EspressoViewActionType.TYPE_TEXT);
        setText(stringToBeTyped);
    }

    public void setText(String stringToBeTyped) {
        this.stringToBeTyped = stringToBeTyped;

        if (!stringToBeTyped.endsWith("\n")) {
            // Appending a \n to the end of the string translates to a ENTER key event.
            this.stringToBeTyped += "\n";
        }
    }

    @Override
    public ViewAction getViewAction() {
        return typeText(stringToBeTyped);
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        return getViewAction().getConstraints().matches(view);
    }

    @Override
    public String getCode() {
        return String.format("typeText(%s)", boxString(stringToBeTyped));
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.action.ViewActions.typeText");
        return imports;
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
