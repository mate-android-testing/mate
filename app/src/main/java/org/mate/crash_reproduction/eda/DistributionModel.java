package org.mate.crash_reproduction.eda;

import org.mate.crash_reproduction.eda.univariate.CGA;
import org.mate.crash_reproduction.eda.univariate.GraphModelRepresentation;
import org.mate.crash_reproduction.eda.univariate.ModelRepresentation;
import org.mate.crash_reproduction.eda.univariate.PIPE;
import org.mate.crash_reproduction.eda.univariate.StoatProbabilityInitialization;
import org.mate.crash_reproduction.eda.univariate.TreeRepresentation;
import org.mate.crash_reproduction.eda.univariate.UMDA;
import org.mate.crash_reproduction.fitness.CrashDistance;
import org.mate.exploration.genetic.selection.FitnessSelectionFunction;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum DistributionModel {
    PIPE_TREE {
        @Override
        public IDistributionModel get() {
            return DistributionModel.getPipe(TreeRepresentation::new);
        }
    },
    PIPE_GRAPH {
        @Override
        public IDistributionModel get() {
            return DistributionModel.getPipe(init -> new GraphModelRepresentation<>(((prevScreenState, currentScreenState) -> prevScreenState == null ? currentScreenState : prevScreenState), init));
        }
    },
    CGA {
        @Override
        public IDistributionModel get() {
            return new CGA(new CrashDistance());
        }
    },
    UMDA {
        @Override
        public IDistributionModel get() {
            return new UMDA(Collections.singletonList(new CrashDistance()), new FitnessSelectionFunction<>());
        }
    };

    public abstract IDistributionModel get();

    private static IDistributionModel getPipe(Function<BiFunction<List<Action>, IScreenState, Map<Action, Double>>, ModelRepresentation> modelRepresentationFunction) {
        // default learning rate 0.01
        // default epsilon 0.000001
        // default clr 0.1
        // default pEl 0.01
        // default pMutation 0.4
        // default mutationRate 0.4
        return new PIPE(modelRepresentationFunction.apply(new StoatProbabilityInitialization(0.6)), new CrashDistance(), 0.01, 0.000001, 0.1, 0.01, 0.4, 0.4);
    }
}
