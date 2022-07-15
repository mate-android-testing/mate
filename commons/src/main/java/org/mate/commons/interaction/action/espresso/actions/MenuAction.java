package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.pressMenuKey;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a Menu key Espresso action.
 */
public class MenuAction extends EspressoViewAction {
    public MenuAction() {
        super(EspressoViewActionType.MENU);
    }

    @Override
    public ViewAction getViewAction() {
        return pressMenuKey();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        // This action can only be performed on the root view.
        return isRoot().matches(view);
    }

    @Override
    public String getCode() {
        return "pressMenuKey()";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.action.ViewActions.pressMenuKey");
        return imports;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected MenuAction(Parcel in) {
        this();
    }

    public static final Creator<MenuAction> CREATOR = new Creator<MenuAction>() {
        @Override
        public MenuAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (MenuAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public MenuAction[] newArray(int size) {
            return new MenuAction[size];
        }
    };
}
