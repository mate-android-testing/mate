package org.mate.crash_reproduction.eda.representation.initializer;

import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public enum Initializer {
    STOAT {
        @Override
        public BiFunction<List<Action>, IScreenState, Map<Action, Double>> get() {
            return new StoatProbabilityInitialization(0.6);
        }
    },
    UNIFORM {
        @Override
        public BiFunction<List<Action>, IScreenState, Map<Action, Double>> get() {
            return new UniformInitialization();
        }
    };

    public abstract BiFunction<List<Action>, IScreenState, Map<Action, Double>> get();
}
