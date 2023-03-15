package org.mate.crash_reproduction.eda.representation.initializer;

import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UniformInitialization implements BiFunction<List<Action>, IScreenState, Map<Action, Double>> {
    @Override
    public Map<Action, Double> apply(List<Action> actions, IScreenState screenState) {
        Set<Action> uniqueActions = new HashSet<>(screenState.getActions());
        return uniqueActions.stream().collect(Collectors.toMap(Function.identity(), a -> 1D / uniqueActions.size()));
    }
}
