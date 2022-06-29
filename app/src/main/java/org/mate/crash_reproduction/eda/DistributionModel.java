package org.mate.crash_reproduction.eda;

import org.mate.crash_reproduction.eda.univariate.CGA;
import org.mate.crash_reproduction.eda.univariate.GraphModelRepresentation;
import org.mate.crash_reproduction.eda.univariate.IModelRepresentation;
import org.mate.crash_reproduction.eda.univariate.PIPE;
import org.mate.crash_reproduction.eda.univariate.StoatProbabilityInitialization;
import org.mate.crash_reproduction.eda.univariate.TreeRepresentation;
import org.mate.crash_reproduction.eda.univariate.UMDA;
import org.mate.crash_reproduction.fitness.CrashDistance;
import org.mate.exploration.genetic.selection.FitnessSelectionFunction;
import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum DistributionModel {
    PIPE {
        @Override
        public IDistributionModel get(IModelRepresentation modelRepresentation) {
            // default learning rate 0.01
            // default epsilon 0.000001
            // default clr 0.1
            // default pEl 0.01
            // default pMutation 0.4
            // default mutationRate 0.4
            return new PIPE(modelRepresentation, new CrashDistance(), 0.01, 0.000001, 0.1, 0.01, 0.4, 0.4);
        }
    },
    CGA {
        @Override
        public IDistributionModel get(IModelRepresentation modelRepresentation) {
            return new CGA(modelRepresentation, new CrashDistance());
        }
    },
    UMDA {
        @Override
        public IDistributionModel get(IModelRepresentation modelRepresentation) {
            return new UMDA(modelRepresentation, Collections.singletonList(new CrashDistance()), new FitnessSelectionFunction<>());
        }
    };

    public abstract IDistributionModel get(IModelRepresentation modelRepresentation);
}
