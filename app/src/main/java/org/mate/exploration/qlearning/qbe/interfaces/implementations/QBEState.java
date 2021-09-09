package org.mate.exploration.qlearning.qbe.interfaces.implementations;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.interfaces.State;
import org.mate.exploration.qlearning.qbe.interfaces.StateSkeleton;
import org.mate.state.IScreenState;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class QBEState extends StateSkeleton<QBEAction> implements State<QBEAction> {

    private final IScreenState screenState;

    public QBEState(final IScreenState screenState) {
        this.screenState = Objects.requireNonNull(screenState);
    }

    public QBEState(final QBEState state) {
        this(state.screenState);
    }

    public Set<QBEAction> getActions() {
        return screenState.getActions().stream().map(QBEAction::new).collect(Collectors.toSet());
    }

    @Override
    protected int getNumberOfComponent() {
        // TODO: Which components?
        return 0;
    }

    @Override
    protected Map<String, Integer> getFeatureMap() {
        final Map<String, Long> counts = screenState.getWidgets()
                .stream()
                .map(widget -> widget.getClazz() + "@" + widget.getDepth())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return counts.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue()));
    }
}
