package org.mate.interaction.intent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a single intent-filter tag and the information it contains,
 * i.e. the declared actions, categories and data attributes.
 */
class IntentFilterDescription {

    private Set<String> actions = new HashSet<>();
    private Set<String> categories = new HashSet<>();

    void addAction(String action) {
        actions.add(action);
    }

    void addCategory(String category) {
        categories.add(category);
    }


}
