package org.mate.exploration.rl.qlearning.autoblacktest;

import androidx.annotation.NonNull;

import org.mate.commons.interaction.action.ui.Widget;

import java.util.Objects;
import java.util.Properties;

/**
 * Describes a trait of a widget, i.e. a subset of properties that are representative and invariant.
 */
public class WidgetTrait {

    /**
     * The trait of a widget encoded as key-value pairs.
     */
    private final Properties trait;

    /**
     * Constructs a new trait for the given widget.
     *
     * @param widget The widget for which the trait should be constructed.
     */
    public WidgetTrait(Widget widget) {
        trait = new Properties();
        trait.setProperty("type", widget.getClazz());
        trait.setProperty("bounds", String.valueOf(widget.getBounds()));
        trait.setProperty("resourceID", widget.getResourceID());
        trait.setProperty("description", widget.getContentDesc());
    }

    /**
     * Retrieves the size of the trait, i.e. the number of different properties.
     *
     * @return Returns the trait size.
     */
    public int size() {
        return trait.size();
    }

    /**
     * Checks for equality between this and another trait.
     *
     * @param o The other trait.
     * @return Returns {@code true} if both traits are equal, otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            WidgetTrait other = (WidgetTrait) o;
            return trait.equals(other.trait);
        }
    }

    /**
     * Computes a hash code for the trait.
     *
     * @return Returns the hash code of the trait.
     */
    @Override
    public int hashCode() {
        return Objects.hash(trait);
    }

    /**
     * Provides a simple textual representation of the widget trait.
     *
     * @return Returns the string representation of the trait.
     */
    @NonNull
    @Override
    public String toString() {
        return trait.toString();
    }
}
