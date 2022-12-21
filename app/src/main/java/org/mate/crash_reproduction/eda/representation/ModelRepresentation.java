package org.mate.crash_reproduction.eda.representation;

import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public enum ModelRepresentation {
    TREE {
        @Override
        public IModelRepresentation get(BiFunction<List<Action>, IScreenState, Map<Action, Double>> initializer) {
            return new TreeRepresentation(initializer);
        }
    },
    GRAPH {
        @Override
        public IModelRepresentation get(BiFunction<List<Action>, IScreenState, Map<Action, Double>> initializer) {
            return new GraphModelRepresentation<>((prev, cur) -> prev, initializer);
        }
    };

    public abstract IModelRepresentation get(BiFunction<List<Action>, IScreenState, Map<Action, Double>> initializer);
}
