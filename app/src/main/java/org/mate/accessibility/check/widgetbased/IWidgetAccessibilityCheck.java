package org.mate.accessibility.check.widgetbased;

import org.mate.state.IScreenState;
import org.mate.ui.Widget;

/**
 * Created by marceloeler on 26/06/17.
 */

public interface IWidgetAccessibilityCheck {

    public boolean check(IScreenState state, Widget widget);
    public String getType();
    public String getInfo();
}
