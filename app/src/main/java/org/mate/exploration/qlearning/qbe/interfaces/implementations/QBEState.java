package org.mate.exploration.qlearning.qbe.interfaces.implementations;

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
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class QBEState extends StateSkeleton<QBEAction> implements State<QBEAction> {

    private final Set<QBEAction> actions;
    private final Map<String, Integer> featureMap;

    public QBEState(final IScreenState screenState) {
        Objects.requireNonNull(screenState);
        featureMap = computeFeatureMap(screenState.getWidgets());
        actions = screenState.getActions().stream().map(QBEAction::new).collect(Collectors.toSet());
    }

    public QBEState(final QBEState state) {
        // Note: Shallow copies are ok, because both fields are read-only.
        Objects.requireNonNull(state);
        actions = state.actions;
        featureMap = state.featureMap;
    }

    private static Map<String, Integer> computeFeatureMap(final List<Widget> widgets) {
        final Map<String, Long> counts = widgets.stream()
                .map(widget -> widget.getClazz() + "@" + widget.getDepth())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return counts.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue()));
    }

    public Set<QBEAction> getActions() {
        return Collections.unmodifiableSet(actions);
    }

    @Override
    protected int getNumberOfComponent() {
        // TODO: Which components?
        return 0;
    }

    @Override
    protected Map<String, Integer> getFeatureMap() {
        return Collections.unmodifiableMap(featureMap);
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
