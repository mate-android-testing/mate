package org.mate.utils.input_generation;

import android.support.test.uiautomator.UiDevice;

import org.mate.MATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Saves the fragments belonging to an activity.
 */
public class FragmentStore {

    /**
     * A map with the activity as key and their containing fragments.
     */
    private final Map<String, List<String>> fragmentsForActivity;

    private static FragmentStore store;

    /**
     * Private constructor for singleton pattern.
     */
    private FragmentStore() {
        fragmentsForActivity = new HashMap<>();
    }

    /**
     * Gets the only existing instance of {@link FragmentStore}.
     *
     * @return The existing instance.
     */
    public static FragmentStore getInstance() {
        if (store == null) {
            store = new FragmentStore();
        }
        return store;
    }

    /**
     * Returns all occurring fragments on an activity. If fragments of this activity have already
     * been searched for before, {@link FragmentStore#fragmentsForActivity} is searched for.
     * Otherwise, an adb shell command is issued. Duplicates are possible.
     *
     * @param device The current {@link UiDevice}.
     * @param activity The current activity.
     * @return A list of the fragments for this activity. (Duplicates are possible)
     */
    public List<String> getFragmentsFor(UiDevice device, String activity) {
        if (fragmentsForActivity.containsKey(activity)) {
            return fragmentsForActivity.get(activity);
        }
        List<String> newFragments = getCurrentFragments(device, activity);
        fragmentsForActivity.put(activity, newFragments);
        return newFragments;
    }

    /**
     * Returns the currently visible fragments for an emulator..
     *
     * @return Returns the currently visible fragments.
     */
    private List<String> getCurrentFragments(UiDevice device, String activity) {

        // TODO: It seems that the extraction can be simplified by supplying the current activity:
        //  (Comment Severin: Same output as Registry.getPackageName)
        // https://stackoverflow.com/questions/24429049/get-info-of-current-visible-fragments-in-android-dumpsys

        try {
            String output = device.executeShellCommand("dumpsys activity " + activity);

            output = output.split("Local FragmentActivity")[1];
            output = output.split("Added Fragments:")[1];
            output = output.split("Back Stack Index:")[0];
            List<String> fragments = Arrays.stream(output.split("\n|\\s")).collect(Collectors.toList());
            fragments.removeIf(s -> s.equals(""));
            fragments.removeIf(s -> s.startsWith("#"));
            fragments.removeIf(s -> s.startsWith("id"));
            fragments.removeIf(s -> s.startsWith("("));
            List<String> extracted = new ArrayList<>();
            for (String s : fragments) {
                extracted.add(s.split("\\{")[0]);
            }
            return extracted;
        } catch (Exception e) {
            MATE.log_warn("Couldn't retrieve currently active fragments: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
