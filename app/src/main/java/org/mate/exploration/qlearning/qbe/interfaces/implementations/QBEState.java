package org.mate.exploration.qlearning.qbe.interfaces.implementations;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.interfaces.State;
import org.mate.exploration.qlearning.qbe.interfaces.StateSkeleton;
import org.mate.interaction.action.ui.Widget;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class QBEState extends StateSkeleton<QBEAction> implements State<QBEAction> {

    private final Set<QBEAction> actions;
    private final Map<String, Integer> featureMap;
    private int numberOfComponents;

    public QBEState(final IScreenState screenState) {
        Objects.requireNonNull(screenState);
        featureMap = computeFeatureMap(screenState.getWidgets());
        actions = screenState.getActions().stream().map(QBEAction::new).collect(toSet());
        numberOfComponents = featureMap.values().stream().mapToInt(i -> i).sum();
    }

    public QBEState(final QBEState state) {
        // Note: Shallow copies are ok, because both fields are read-only.
        Objects.requireNonNull(state);
        actions = state.actions;
        featureMap = state.featureMap;
        numberOfComponents = state.numberOfComponents;
    }

    private static Map<String, Integer> computeFeatureMap(final List<Widget> widgets) {
        final Map<String, Long> counts = widgets.stream()
                .map(widget -> widget.getClazz() + "@" + widget.getDepth())
                .collect(groupingBy(Function.identity(), counting()));
        return counts.entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().intValue()));
    }

    public Set<QBEAction> getActions() {
        return Collections.unmodifiableSet(actions);
    }

    @Override
    protected int getNumberOfComponent() {
        return numberOfComponents;
    }

    @Override
    public Map<String, Integer> getFeatureMap() {
        return Collections.unmodifiableMap(featureMap);
    }

    public void addDummyComponent() {
        featureMap.merge("dummyComponent@-1", 0, (value, ignored) -> value + 1);
        ++numberOfComponents;
    }

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
