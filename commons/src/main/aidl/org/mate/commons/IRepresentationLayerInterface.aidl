// IRepresentationLayerInterface.aidl
package org.mate.commons;

import org.mate.commons.interaction.action.ui.Widget;

interface IRepresentationLayerInterface {
    // Representation Layer Configuration
    void setTargetPackageName(String packageName);

    // General device info
    int getDisplayWidth();
    int getDisplayHeight();
    boolean isCrashDialogDisplayed();

    // Activities info
    String getCurrentPackageName();
    String getCurrentActivityName();
    List<String> getTargetPackageActivityNames();

    // Widget actions
    List<Widget> getCurrentScreenWidgets();
}