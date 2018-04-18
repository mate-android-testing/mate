package org.mate.accessibility.check;

import org.mate.state.IScreenState;
import org.mate.ui.Widget;

/**
 * Created by marceloeler on 26/06/17.
 */

public interface IWidgetAccessibilityCheck {

    public boolean check(Widget widget);
}
