package org.mate.exploration.qlearning.qbe.abstractions.state;

import org.mate.exploration.qlearning.qbe.abstractions.action.QBEAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * Defines a state in the context of QBE.
 */
public final class QBEState extends AbstractQBEState<QBEAction> implements State<QBEAction> {

    /**
     * The applicable actions in the current state.
     */
    private final Set<QBEAction> actions;

    /**
     * A mapping from (widget type, widget depth) to its relative count.
     */
    private final Map<String, Integer> featureMap;

    /**
     * The number of GUI components on the underlying screen.
     */
    private int numberOfComponents;

    /**
     * Initialises a new state.
     *
     * @param screenState The underlying screen state.
     */
    public QBEState(final IScreenState screenState) {
        Objects.requireNonNull(screenState);
        featureMap = computeFeatureMap(screenState.getWidgets());
        actions = Collections.unmodifiableSet(screenState.getUIActions().stream()
                .map(QBEAction::new)
                .collect(toSet()));
        numberOfComponents = featureMap.values().stream().mapToInt(i -> i).sum();
    }

    /**
     * Used to clone a state (copy constructor).
     *
     * @param state The state to be cloned.
     */
    public QBEState(final QBEState state) {
        Objects.requireNonNull(state);
        actions = state.actions;
        featureMap = new HashMap<>(state.featureMap);
        numberOfComponents = state.numberOfComponents;
    }

    /**
     * Computes the feature map from the given list of widgets, which is constructed as follows:
     * The widgets are grouped based on their type, e.g. button, and their depth in the ui tree
     * and mapped to their relative count.
     *
     * @param widgets The list of widgets.
     * @return Returns the computed feature map.
     */
    private Map<String, Integer> computeFeatureMap(final List<Widget> widgets) {
        final Map<String, Long> counts = widgets.stream()
                .map(widget -> widget.getClazz() + "@" + widget.getDepth())
                .collect(groupingBy(Function.identity(), counting()));
        return counts.entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().intValue()));
    }

    /**
     * Retrieves the actions applicable on the given state.
     *
     * @return Returns the applicable actions.
     */
    public Set<QBEAction> getActions() {
        return Collections.unmodifiableSet(actions);
    }

    /**
     * Retrieves the number of GUI components on the underlying screen.
     *
     * @return Returns the number of GUI components.
     */
    @Override
    protected int getNumberOfComponents() {
        return numberOfComponents;
    }

    /**
     * Returns the feature map.
     *
     * @return Returns the feature map.
     */
    @Override
    public Map<String, Integer> getFeatureMap() {
        return Collections.unmodifiableMap(featureMap);
    }

    /**
     *
     */
    public void addDummyComponent() {
        featureMap.merge("dummyComponent@-1", 1, (value, ignored) -> value + 1);
        ++numberOfComponents;
    }

    /**
     * Defines a custom string representation for a {@link QBEState}. Note that this format
     * is reflected in the serialized transition system, see
     * {@link org.mate.exploration.qlearning.qbe.transition_system.TransitionSystemSerializer}.
     *
     * @return Returns the string representation of the state.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{\"actions\":[");
        boolean firstEntry = true;
        for (final QBEAction action : actions) {
            if (!firstEntry) {
                sb.append(",");
            } else {
                firstEntry = false;
            }
            sb.append(action);
        }
        sb.append("],\"featureMap\":{");
        firstEntry = true;
        for (final Map.Entry<String, Integer> entry : featureMap.entrySet()) {
            if (!firstEntry) {
                sb.append(",");
            } else {
                firstEntry = false;
            }
            sb.append("\"");
            sb.append(entry.getKey());
            sb.append("\":");
            sb.append(entry.getValue());
        }
        sb.append("}}");
        return sb.toString();
    }
}
