// IRepresentationLayerInterface.aidl
package org.mate.commons;

import org.mate.commons.interaction.action.ui.Widget;

interface IRepresentationLayerInterface {
    String getCurrentPackageName();
    String getCurrentActivityName();
    List<Widget> getCurrentScreenWidgets();
}