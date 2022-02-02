// IRepresentationLayerInterface.aidl
package org.mate.commons;

import org.mate.commons.interaction.action.ui.Widget;

interface IRepresentationLayerInterface {
    // General device info
    int getDisplayWidth();
    int getDisplayHeight();
    String getCurrentPackageName();
    String getCurrentActivityName();

    // Widget actions
    List<Widget> getCurrentScreenWidgets();
}