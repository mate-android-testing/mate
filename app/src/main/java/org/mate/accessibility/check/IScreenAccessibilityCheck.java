package org.mate.accessibility.check;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.state.IScreenState;

/**
 * Created by marceloeler on 26/06/17.
 */

public interface IScreenAccessibilityCheck {

    public AccessibilityViolation check(IScreenState state);
}
