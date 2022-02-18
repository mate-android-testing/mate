// IRepresentationLayerInterface.aidl
package org.mate.commons;

import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.interaction.action.Action;

interface IRepresentationLayerInterface {
    // Representation Layer status & config
    void ping();
    void exit();
    void setTargetPackageName(String packageName);
    void setRandomSeed(long seed);
    void setReplayMode();
    void setWidgetBasedActions();

    // General device info & config
    int getDisplayWidth();
    int getDisplayHeight();
    boolean grantRuntimePermission(String permission);

    // Activities info
    String getCurrentPackageName();
    String getCurrentActivityName();
    List<String> getTargetPackageActivityNames();

    // Execute actions
    String executeShellCommand(String command);
    boolean executeAction(in Action action);

    // Widget actions
    List<Widget> getCurrentScreenWidgets();
}