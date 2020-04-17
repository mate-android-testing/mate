package org.mate.accessibility.check;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.state.IScreenState;

import java.util.List;

public interface IAccessibilityViolationChecker {
    public List<AccessibilityViolation> runAccessibilityChecks(IScreenState state);
}
