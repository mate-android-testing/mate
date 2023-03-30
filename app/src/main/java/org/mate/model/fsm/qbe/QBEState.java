package org.mate.model.fsm.qbe;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import org.mate.interaction.action.Action;
import org.mate.interaction.action.StartAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.model.fsm.State;
import org.mate.state.IScreenState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class QBEState extends State {

    /**
     * A mapping from (widget type, widget depth) to its relative count.
     */
    private final Map<String, Integer> featureMap;

    /**
     * The number of GUI components on the underlying screen.
     */
    private int numberOfComponents;

    private final boolean isCrashState;

    private final boolean isVirtualRootState;

    QBEState(int id, IScreenState screenState) {
        super(id, screenState);
        featureMap = computeFeatureMap(screenState.getWidgets());
        numberOfComponents = featureMap.values().stream().mapToInt(i -> i).sum();
        isCrashState = false;
        isVirtualRootState = false;
    }

    QBEState(final QBEState other) {
        super(other.id, other.getScreenState());
        featureMap = new HashMap<>(other.featureMap);
        numberOfComponents = other.numberOfComponents;
        isCrashState = other.isCrashState;
        isVirtualRootState = other.isVirtualRootState;
    }

    private QBEState(int id, IScreenState screenState, boolean isCrashState, boolean isVirtualRootState) {
        super(id, screenState);
        assert isCrashState ^ isVirtualRootState;
        featureMap = new HashMap<>(0);
        numberOfComponents = 0;
        this.isCrashState = isCrashState;
        this.isVirtualRootState = isVirtualRootState;
    }

    public static QBEState createCrashState(int id, IScreenState screenState) {
        return new QBEState(id, screenState, true, false);
    }

    public static QBEState createVirtualRootState(int id, IScreenState screenState) {
        return new QBEState(id, screenState, false, true);
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
     * Retrieves the number of GUI components on the underlying screen.
     *
     * @return Returns the number of GUI components.
     */
    public int getNumberOfComponents() {
        return numberOfComponents;
    }

    /**
     * Returns the feature map.
     *
     * @return Returns the feature map.
     */
    public Map<String, Integer> getFeatureMap() {
        return Collections.unmodifiableMap(featureMap);
    }

    public List<Action> getActions() {
        if (isCrashState) {
            return Collections.emptyList();
        }

        if (isVirtualRootState) {
            return new ArrayList<Action>() {{
                new StartAction();
            }};
        }

        return new ArrayList<>(screenState.getUIActions());
    }

    public void addDummyComponent() {
        featureMap.merge("dummyComponent@-1", 1, (value, ignored) -> value + 1);
        ++numberOfComponents;
    }
}
