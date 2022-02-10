// IRepresentationLayerInterface.aidl
package org.mate.commons;

import org.mate.commons.interaction.action.ui.Widget;

interface IRepresentationLayerInterface {
    // Representation Layer status & config
    void ping();
    void exit();
    void setTargetPackageName(String packageName);

    // General device info
    int getDisplayWidth();
    int getDisplayHeight();
    boolean isCrashDialogDisplayed();

    // Activities info
    String getCurrentPackageName();
    String getCurrentActivityName();
    List<String> getTargetPackageActivityNames();

    // AUT config
    boolean clearTargetPackageData();
    boolean restartTargetPackage();

    // General actions
    String executeShellCommand(String command);

    // Widget actions
    List<Widget> getCurrentScreenWidgets();
}