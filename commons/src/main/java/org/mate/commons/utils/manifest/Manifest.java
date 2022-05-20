package org.mate.commons.utils.manifest;

import org.mate.commons.utils.manifest.element.ComponentDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the AndroidManifest that is a mandatory part of each app. See
 * https://developer.android.com/guide/topics/manifest/manifest-intro for more details.
 */
public final class Manifest {

    /**
     * The list of components. A component can either be an activity, a service, a broadcast receiver
     * or a content provider.
     */
    private final List<ComponentDescription> components;

    /**
     * The package name declared in the top row.
     */
    private final String packageName;

    /**
     * The name of the main activity.
     */
    private final String mainActivity;

    // further elements: https://developer.android.com/guide/topics/manifest/manifest-intro

    /**
     * Initialises a new manifest with the given components.
     *
     * @param components The list of components.
     */
    public Manifest(String packageName, List<ComponentDescription> components, String mainActivity) {
        this.packageName = packageName;
        this.components = components;
        this.mainActivity = mainActivity;
    }

    /**
     * Returns the list of components.
     *
     * @return Returns the list of components.
     */
    public List<ComponentDescription> getComponents() {
        return components;
    }

    /**
     * Returns the activities declared in the manifest.
     *
     * @return Returns the activities declared in the manifest.
     */
    public List<ComponentDescription> getActivities() {
        List<ComponentDescription> activities = new ArrayList<>();

        for (ComponentDescription component : components) {
            if (component.isActivity()) {
                activities.add(component);
            }
        }

        return activities;
    }

    /**
     * Returns the exported activities declared in the manifest.
     *
     * @return Returns the list of exported activities of the manifest.
     */
    public List<ComponentDescription> getExportedActivities() {
        List<ComponentDescription> exportedActivities = new ArrayList<>();

        for (ComponentDescription component : components) {
            if (component.isActivity() && component.isExported() && component.isEnabled()) {
                exportedActivities.add(component);
            }
        }

        return exportedActivities;
    }

    /**
     * Returns the package name.
     *
     * @return Returns the package name.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns the name of the main activity.
     *
     * @return Returns the name of the main activity.
     */
    public String getMainActivity() {
        return mainActivity;
    }
}
