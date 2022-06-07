package org.mate.commons.interaction.action.espresso;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.mate.commons.utils.MATELog;

import java.util.Objects;
import java.util.UUID;

/**
 * Wrapper around the View class.
 * It provides useful information for building Espresso ViewMatchers and ViewActions.
 */
public class EspressoView {
    // An auto-generated random UUID, used to compare different instances of EspressoView.
    // Note that this value will be different for different instances constructed with the same
    // View.
    private final UUID randomUUID;
    private View view;

    public EspressoView(View view) {
        this.view = view;
        this.randomUUID = UUID.randomUUID();
    }

    public View getView() {
        return view;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EspressoView that = (EspressoView) o;
        return randomUUID.equals(that.randomUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(randomUUID);
    }

    public Integer getId() {
        return view.getId();
    }

    public String getClassName() {
        return view.getClass().getName();
    }

    public @Nullable
    String getContentDescription() {
        CharSequence contentDescription = view.getContentDescription();
        if (contentDescription != null) {
            return contentDescription.toString();
        }

        return "";
    }

    public @Nullable String getText() {
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            if (text != null) {
                return text.toString();
            }
        }

        return "";
    }

    public @Nullable String getResourceName(Context autContext) {
        int id = view.getId();
        if (View.NO_ID == id) {
            return null;
        }

        try {
            return autContext.getResources().getResourceName(id);
        } catch (Resources.NotFoundException e) {
            MATELog.log_warn(String.format("Unable to find resource name for view with id %d", id));
            return null;
        }
    }

    /**
     * Returns a boolean indicating whether this view is an Android view (e.g., created by the
     * OS) or not.
     */
    public boolean isAndroidView(Context autContext) {
        String resourceName = getResourceName(autContext);
        if (resourceName == null) {
            return false;
        }

        return resourceName.startsWith("android")
                || resourceName.startsWith("com.google.android")
                || resourceName.startsWith("com.android");
    }
}
