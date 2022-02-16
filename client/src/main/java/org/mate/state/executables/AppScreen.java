package org.mate.state.executables;

import android.graphics.Rect;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.utils.MATELog;
import org.mate.interaction.DeviceMgr;
import org.mate.interaction.EnvironmentManager;
import org.mate.service.MATEService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Models an app screen with all (discoverable) widgets on it.
 */
public class AppScreen {

    /**
     * Defines the activity name that corresponds to the app screen.
     */
    private final String activityName;

    /**
     * Defines the package name that corresponds to the app screen.
     */
    private final String packageName;

    /**
     * A list of discovered widgets on the app screen.
     */
    private List<Widget> widgets;

    /**
     * Stores relevant information about the device, e.g. the display height.
     * Also enables the interaction with the device, e.g. perform a click on some ui element.
     */
    private final DeviceMgr deviceMgr;

    /**
     * Creates a new app screen containing the widgets on it.
     */
    public AppScreen(DeviceMgr deviceMgr) {
        this.deviceMgr = deviceMgr;

        this.activityName = this.deviceMgr.getCurrentActivity();

        if (activityName.equals(EnvironmentManager.ACTIVITY_UNKNOWN)) {
            this.packageName = this.deviceMgr.getCurrentPackageName();
        } else {
            this.packageName = activityName.split("/")[0];
        }

        // retrieve widgets from current screen
        try {
            MATELog.log_debug("AppScreen: " + activityName);
            this.widgets = MATEService.getRepresentationLayer().getCurrentScreenWidgets();
            MATELog.log_debug("Number of widgets: " + widgets.size());
        } catch (RemoteException e) {
            e.printStackTrace();
            this.widgets = new ArrayList<>();
        }
    }

    /**
     * Returns the activity name that app screen corresponds to.
     *
     * @return Returns the activity name.
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * Returns the widgets linked to the app screen.
     *
     * @return Returns the widgets of the app screen.
     */
    public List<Widget> getWidgets() {
        return Collections.unmodifiableList(widgets);
    }

    /**
     * Returns the package name the app screens corresponds to.
     *
     * @return Returns the package name.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns the bounding box of the status bar. This depends on display width
     * and has a fixed height of 72 pixels, e.g. [0,0][1080][72].
     *
     * @return Returns the bounding box of the status bar.
     */
    public Rect getStatusBarBoundingBox() {
        return new Rect(0, 0, getWidth(), 72);
    }

    /**
     * Returns the bounding box of the app screen. This depends on display width
     * and the display height, e.g. [0,0][1080][1920].
     *
     * @return Returns the bounding box of the app screen.
     */
    @SuppressWarnings("unused")
    public Rect getBoundingBox() {
        return new Rect(0, 0, getWidth(), getHeight());
    }

    /**
     * Returns the screen width.
     *
     * @return Returns the screen width in pixels.
     */
    public int getWidth() {
        return deviceMgr.getScreenWidth();
    }

    /**
     * Returns the screen height.
     *
     * @return Returns the screen height in pixels.
     */
    public int getHeight() {
        return deviceMgr.getScreenHeight();
    }

    /**
     * Compares two app screens for equality.
     *
     * @param o The other app screen to compare against.
     * @return Returns {@code true} if the two app screens are identical,
     * otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            AppScreen appScreen = (AppScreen) o;
            return Objects.equals(activityName, appScreen.activityName) &&
                    Objects.equals(packageName, appScreen.packageName) &&
                    Objects.equals(widgets, appScreen.widgets);
        }
    }

    /**
     * Computes the hash code based on attributes used for {@link #equals(Object)}.
     *
     * @return Returns the associated hash code of the app screen.
     */
    @Override
    public int hashCode() {
        return Objects.hash(activityName, packageName, widgets);
    }

    /**
     * A simple textual representation of the app screen.
     *
     * @return Returns the string representation of the app screen.
     */
    @NonNull
    @Override
    public String toString() {
        return "AppScreen{activity: " + activityName + ", widgets: " + widgets.size() + "}";
    }
}
